package com.tuum.corebanking.messaging.publisher.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private DirectExchange exchange;

    @InjectMocks
    private RabbitMQEventPublisher publisher;

    @Test
    void publishEventSuccessfully() {
        String exchangeName = "test-exchange";
        String routingKey = "test.routing.key";
        Object event = new Object();

        when(exchange.getName()).thenReturn(exchangeName);

        publisher.publish(routingKey, event);

        verify(exchange, times(1)).getName();
        verify(rabbitTemplate, times(1)).convertAndSend(exchangeName, routingKey, event);
    }

    @Test
    void publishEventWithNullRoutingKey() {
        String exchangeName = "test-exchange";
        Object event = new Object();

        when(exchange.getName()).thenReturn(exchangeName);

        publisher.publish(null, event);

        verify(exchange, times(1)).getName();
        verify(rabbitTemplate, times(1)).convertAndSend(exchangeName, (String) null, event);
    }

    @Test
    void publishEventWithNullEvent() {
        String exchangeName = "test-exchange";
        String routingKey = "test.routing.key";

        when(exchange.getName()).thenReturn(exchangeName);

        publisher.publish(routingKey, null);

        verify(exchange, times(1)).getName();
        verify(rabbitTemplate, times(1)).convertAndSend(exchangeName, routingKey, (Object) null);
    }

    @Test
    void publishEventPropagatesExceptionWhenRabbitTemplateFails() {
        String exchangeName = "test-exchange";
        String routingKey = "test.routing.key";
        Object event = new Object();

        when(exchange.getName()).thenReturn(exchangeName);
        doThrow(new AmqpException("Simulated AMQP Failure"))
                .when(rabbitTemplate)
                .convertAndSend(exchangeName, routingKey, event);

        assertThrows(AmqpException.class, () -> publisher.publish(routingKey, event));
        verify(exchange, times(1)).getName();
        verify(rabbitTemplate, times(1)).convertAndSend(exchangeName, routingKey, event);
    }
}