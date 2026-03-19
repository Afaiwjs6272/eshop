package ru.ukhanov.shop.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.ukhanov.shop.dto.enums.OrderEventType
import ru.ukhanov.shop.dto.enums.UserEventType
import java.math.BigDecimal
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = UserEvent::class, name = "USER_EVENT"),
    JsonSubTypes.Type(value = OrderEvent::class, name = "ORDER_EVENT")
)
sealed class KafkaEvent {
    abstract val eventId: String
    abstract val timestamp: Instant
}

data class UserEvent(
    override val eventId: String,
    override val timestamp: Instant = Instant.now(),
    val userId: Long,
    val userEmail: String,
    val eventType: UserEventType,
    val userData: UserDto?
) : KafkaEvent()

data class OrderEvent(
    override val eventId: String,
    override val timestamp: Instant = Instant.now(),
    val orderId: Long,
    val orderNumber: String,
    val userId: Long,
    val eventType: OrderEventType,
    val amount: BigDecimal,
    val status: String
) : KafkaEvent()