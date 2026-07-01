package com.tuum.corebanking.messaging.publisher;

public interface EventPublisher {
    void publish(String routingKey, Object event);
}