package ru.ukhanov.shop.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import ru.ukhanov.shop.dto.OrderEvent
import ru.ukhanov.shop.dto.UserEvent
import java.util.concurrent.CompletableFuture

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendUserEvent(event: UserEvent): CompletableFuture<SendResult<String, Any>> {
        return sendEvent("user-events", event.userId.toString(), event)
    }

    fun sendOrderEvent(event: OrderEvent): CompletableFuture<SendResult<String, Any>> {
        return sendEvent("order-events", event.orderId.toString(), event)
    }

    private fun sendEvent(topic: String, key: String, event: Any): CompletableFuture<SendResult<String, Any>> {
        log.debug("Sending event to topic {}: {}", topic, event)

        val future = kafkaTemplate.send(topic, key, event)

        future.whenComplete { result, ex ->
            if (ex == null) {
                log.info("Event sent successfully to topic {}: partition={}, offset={}",
                    topic, result.recordMetadata.partition(), result.recordMetadata.offset())
            } else {
                log.error("Failed to send event to topic {}: {}", topic, ex.message, ex)
            }
        }

        return future
    }

    fun sendBulkEvents(topic: String, events: List<Pair<String, Any>>): List<CompletableFuture<SendResult<String, Any>>> {
        return events.map { (key, event) -> sendEvent(topic, key, event) }
    }
}