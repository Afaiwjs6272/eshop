package ru.ukhanov.shop.mapper

import org.springframework.stereotype.Component
import ru.ukhanov.shop.dto.OrderDto
import ru.ukhanov.shop.model.Order

@Component
class OrderMapper {
    fun toDto(order: Order) = OrderDto(
        id = order.id,
        orderNumber = order.orderNumber,
        description = order.description,
        amount = order.amount,
        status = order.status,
        userId = order.user.id!!,
        createdAt = order.createdAt,
        updatedAt = order.updatedAt
    )
}