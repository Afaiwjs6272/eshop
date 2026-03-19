package ru.ukhanov.shop.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ukhanov.shop.dto.OrderDto
import ru.ukhanov.shop.model.enums.OrderStatus
import ru.ukhanov.shop.service.order.OrderService

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun createOrder(@Valid @RequestBody orderDto: OrderDto): ResponseEntity<OrderDto> {
        val createdOrder = orderService.createOrder(orderDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder)
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable id: Long): ResponseEntity<OrderDto> {
        val order = orderService.getOrderById(id)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/user/{userId}")
    fun getOrdersByUserId(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<OrderDto>> {
        val orders = orderService.getOrdersByUserId(userId, page, size)
        return ResponseEntity.ok(orders)
    }

    @PatchMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: Long,
        @RequestParam status: OrderStatus
    ): ResponseEntity<OrderDto> {
        val updatedOrder = orderService.updateOrderStatus(id, status)
        return ResponseEntity.ok(updatedOrder)
    }

    @PostMapping("/{id}/cancel")
    fun cancelOrder(@PathVariable id: Long): ResponseEntity<OrderDto> {
        val cancelledOrder = orderService.cancelOrder(id)
        return ResponseEntity.ok(cancelledOrder)
    }

    @GetMapping("/user/{userId}/statistics")
    fun getUserOrderStatistics(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        val statistics = orderService.getUserOrderStatistics(userId)
        return ResponseEntity.ok(statistics)
    }

    @PostMapping("/bulk-status-update")
    fun bulkUpdateOrdersStatus(
        @RequestBody request: BulkStatusUpdateRequest
    ): ResponseEntity<Map<String, Int>> {
        val updatedCount = orderService.bulkUpdateOrdersStatus(request.orderIds, request.newStatus)
        return ResponseEntity.ok(mapOf("updatedCount" to updatedCount))
    }
}

data class BulkStatusUpdateRequest(
    val orderIds: List<Long>,
    val newStatus: OrderStatus
)