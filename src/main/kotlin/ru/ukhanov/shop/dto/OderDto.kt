package ru.ukhanov.shop.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.ukhanov.shop.model.enums.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDto(
    val id: Long? = null,

    @field:NotBlank(message = "Order number is required")
    val orderNumber: String,

    @field:NotBlank(message = "Description is required")
    val description: String,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val amount: BigDecimal,

    val status: OrderStatus = OrderStatus.PENDING,

    @field:NotNull(message = "User ID is required")
    val userId: Long,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val createdAt: LocalDateTime? = null,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val updatedAt: LocalDateTime? = null
)