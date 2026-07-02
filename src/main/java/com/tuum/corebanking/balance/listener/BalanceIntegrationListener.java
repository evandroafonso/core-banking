package com.tuum.corebanking.balance.listener;

import com.tuum.corebanking.balance.event.BalanceCreatedEvent;
import com.tuum.corebanking.balance.event.BalanceUpdateEvent;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BalanceIntegrationListener {

    private final EventPublisher eventPublisher;

    public BalanceIntegrationListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBalanceUpdateEvent(BalanceUpdateEvent event) {
        String routingKey = "balance.%s".formatted(event.operationType().name().toLowerCase());
        eventPublisher.publish(routingKey, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBalanceCreatedEvent(BalanceCreatedEvent event) {
        String routingKey = "balance.%s".formatted(event.operationType().name().toLowerCase());
        eventPublisher.publish(routingKey, event);
    }
}