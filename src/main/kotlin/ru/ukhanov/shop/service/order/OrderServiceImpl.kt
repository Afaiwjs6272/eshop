package ru.ukhanov.shop.service.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ukhanov.shop.annotation.MonitorPerformance
import ru.ukhanov.shop.dto.OrderDto
import ru.ukhanov.shop.dto.OrderEvent
import ru.ukhanov.shop.dto.enums.OrderEventType
import ru.ukhanov.shop.exception.InvalidOperationException
import ru.ukhanov.shop.exception.ResourceNotFoundException
import ru.ukhanov.shop.mapper.OrderMapper
import ru.ukhanov.shop.model.Order
import ru.ukhanov.shop.model.enums.OrderStatus
import ru.ukhanov.shop.repository.OrderRepository
import ru.ukhanov.shop.repository.UserRepository
import ru.ukhanov.shop.service.KafkaProducerService
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class OrderServiceImpl (
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val orderMapper: OrderMapper
): OrderService {

    @Transactional
    @MonitorPerformance
    override fun createOrder(orderDto: OrderDto): OrderDto {
        val user = userRepository.findById(orderDto.userId)
            .orElseThrow { ResourceNotFoundException("User with id ${orderDto.userId} not found") }

        if (orderRepository.findByOrderNumber(orderDto.orderNumber) != null) {
            throw InvalidOperationException("Order with number ${orderDto.orderNumber} already exists")
        }

        val order = Order(
            orderNumber = orderDto.orderNumber,
            description = orderDto.description,
            amount = orderDto.amount,
            status = OrderStatus.PENDING,
            user = user
        )

        val savedOrder = orderRepository.save(order)
        user.orders.add(savedOrder)

        kafkaProducerService.sendOrderEvent(
            OrderEvent(
                eventId = UUID.randomUUID().toString(),
                orderId = savedOrder.id!!,
                orderNumber = savedOrder.orderNumber,
                userId = user.id!!,
                eventType = OrderEventType.CREATED,
                amount = savedOrder.amount,
                status = savedOrder.status.name
            )
        )

        return orderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    @MonitorPerformance
    override fun getOrderById(id: Long): OrderDto {
        return orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order with id $id not found") }
            .let { orderMapper.toDto(it) }
    }

    @Transactional(readOnly = true)
    override fun getOrdersByUserId(userId: Long, page: Int, size: Int): Page<OrderDto> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return orderRepository.findUserOrdersWithPagination(userId, pageable)
            .map { orderMapper.toDto(it) }
    }

    @Transactional
    @MonitorPerformance
    override fun updateOrderStatus(id: Long, newStatus: OrderStatus): OrderDto {
        val order = orderRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Order with id $id not found") }

        validateStatusTransition(order.status, newStatus)

        order.status = newStatus
        order.updatedAt = LocalDateTime.now()

        val updatedOrder = orderRepository.save(order)

        val eventType = when (newStatus) {
            OrderStatus.COMPLETED -> OrderEventType.COMPLETED
            OrderStatus.CANCELLED -> OrderEventType.CANCELLED
            else -> OrderEventType.UPDATED
        }

        kafkaProducerService.sendOrderEvent(
            OrderEvent(
                eventId = UUID.randomUUID().toString(),
                orderId = updatedOrder.id!!,
                orderNumber = updatedOrder.orderNumber,
                userId = updatedOrder.user.id!!,
                eventType = eventType,
                amount = updatedOrder.amount,
                status = updatedOrder.status.name
            )
        )

        return orderMapper.toDto(updatedOrder)
    }

    @Transactional
    @MonitorPerformance
    override fun cancelOrder(id: Long): OrderDto {
        return updateOrderStatus(id, OrderStatus.CANCELLED)
    }

    @Transactional(readOnly = true)
    override fun getUserOrderStatistics(userId: Long): Map<String, Any> {
        val totalSpent = orderRepository.getTotalSpentByUser(userId) ?: BigDecimal.ZERO
        val orders = orderRepository.findByUserId(userId)

        return mapOf(
            "totalOrders" to orders.size,
            "totalSpent" to totalSpent,
            "pendingOrders" to orders.count { it.status == OrderStatus.PENDING },
            "completedOrders" to orders.count { it.status == OrderStatus.COMPLETED },
            "cancelledOrders" to orders.count { it.status == OrderStatus.CANCELLED },
            "averageOrderValue" to if (orders.isNotEmpty()) {
                totalSpent.divide(BigDecimal(orders.size), 2, java.math.RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
        )
    }

    @Transactional
    override fun bulkUpdateOrdersStatus(orderIds: List<Long>, newStatus: OrderStatus): Int {
        return orderRepository.bulkUpdateOrderStatus(orderIds, newStatus)
    }

    private fun validateStatusTransition(currentStatus: OrderStatus, newStatus: OrderStatus) {
        val validTransitions = mapOf(
            OrderStatus.PENDING to setOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING to setOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.COMPLETED to emptySet(),
            OrderStatus.CANCELLED to emptySet()
        )

        if (newStatus !in validTransitions[currentStatus]!!) {
            throw InvalidOperationException(
                "Cannot transition from $currentStatus to $newStatus"
            )
        }
    }
}