package ru.ukhanov.shop.consumer

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.ukhanov.shop.dto.OrderEvent
import ru.ukhanov.shop.dto.UserEvent
import ru.ukhanov.shop.dto.enums.OrderEventType
import ru.ukhanov.shop.dto.enums.UserEventType

@Service
class KafkaConsumerService {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["user-events"],
        groupId = "demo-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeUserEvent(
        @Payload event: UserEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        ack: Acknowledgment
    ) {
        log.info("Received user event: {} from partition: {}, offset: {}", event, partition, offset)

        try {
            when (event.eventType) {
                UserEventType.CREATED -> {
                    log.info("User created: {} with email: {}", event.userId, event.userEmail)
                }
                UserEventType.UPDATED -> {
                    log.info("User updated: {}", event.userId)
                }
                UserEventType.DELETED -> {
                    log.info("User deleted: {}", event.userId)
                }
            }

            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Error processing user event: {}", e.message, e)
            ack.acknowledge()
        }
    }

    @KafkaListener(
        topics = ["order-events"],
        groupId = "demo-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeOrderEvent(
        @Payload event: OrderEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        ack: Acknowledgment
    ) {
        log.info("Received order event: {} from partition: {}, offset: {}", event, partition, offset)

        try {
            when (event.eventType) {
                OrderEventType.CREATED -> {
                    log.info("Order created: {} for user: {}", event.orderNumber, event.userId)
                }
                OrderEventType.COMPLETED -> {
                    log.info("Order completed: {}", event.orderNumber)
                }
                OrderEventType.CANCELLED -> {
                    log.info("Order cancelled: {}", event.orderNumber)
                }
                OrderEventType.UPDATED -> {
                    log.info("Order updated: {}", event.orderNumber)
                }
            }

            ack.acknowledge()
        } catch (e: Exception) {
            log.error("Error processing order event: {}", e.message, e)
            ack.acknowledge()
        }
    }
}