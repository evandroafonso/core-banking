package com.tuum.corebanking.messaging.publisher.impl;

import com.tuum.corebanking.messaging.publisher.EventPublisher;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventPublisher implements EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange exchange;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate, DirectExchange exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    @Override
    public void publish(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange.getName(), routingKey, event);
    }
}