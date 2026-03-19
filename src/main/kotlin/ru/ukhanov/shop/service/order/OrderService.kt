package ru.ukhanov.shop.service.order

import org.springframework.data.domain.Page
import ru.ukhanov.shop.dto.OrderDto
import ru.ukhanov.shop.model.enums.OrderStatus

interface OrderService {
    fun createOrder(orderDto: OrderDto): OrderDto

    fun getOrderById(id: Long): OrderDto

    fun getOrdersByUserId(userId: Long, page: Int, size: Int): Page<OrderDto>

    fun updateOrderStatus(id: Long, newStatus: OrderStatus): OrderDto

    fun cancelOrder(id: Long): OrderDto

    fun getUserOrderStatistics(userId: Long): Map<String, Any>

    fun bulkUpdateOrdersStatus(orderIds: List<Long>, newStatus: OrderStatus): Int
}