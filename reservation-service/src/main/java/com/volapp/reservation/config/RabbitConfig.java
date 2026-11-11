package com.volapp.reservation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ : exchange topic "volapp.evenements" et convertisseur JSON.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "volapp.evenements";

    @Bean
    public TopicExchange volappEvenements() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter convertisseurJson(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
