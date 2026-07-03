package com.tuum.corebanking.balance.listener;

import com.tuum.corebanking.balance.event.BalanceCreatedEvent;
import com.tuum.corebanking.balance.event.BalanceUpdateEvent;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class BalanceIntegrationListener {

    private final EventPublisher eventPublisher;

    public BalanceIntegrationListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBalanceUpdateEvent(BalanceUpdateEvent event) {
        log.info("Handling BalanceUpdateEvent for currency: {}", event.payload().currency());
        String routingKey = "balance.%s".formatted(event.operationType().name().toLowerCase());
        eventPublisher.publish(routingKey, event);
        log.debug("BalanceUpdateEvent published with routing key: {}", routingKey);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBalanceCreatedEvent(BalanceCreatedEvent event) {
        log.info("Handling BalanceCreatedEvent for currency: {}", event.payload().currency());
        String routingKey = "balance.%s".formatted(event.operationType().name().toLowerCase());
        eventPublisher.publish(routingKey, event);
        log.debug("BalanceCreatedEvent published with routing key: {}", routingKey);
    }
}