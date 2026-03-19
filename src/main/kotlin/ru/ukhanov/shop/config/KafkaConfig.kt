package ru.ukhanov.shop.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.support.converter.StringJsonMessageConverter

@Configuration
class KafkaConfig {

    @Bean
    fun userEventsTopic(): NewTopic = TopicBuilder
        .name("user-events")
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun orderEventsTopic(): NewTopic = TopicBuilder
        .name("order-events")
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun messageConverter(): StringJsonMessageConverter = StringJsonMessageConverter()
}