package com.tuum.corebanking.account.listener;

import com.tuum.corebanking.account.event.AccountCreatedEvent;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AccountIntegrationListener {

    private final EventPublisher eventPublisher;

    public AccountIntegrationListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        String routingKey = "account." + event.operationType().name().toLowerCase();
        eventPublisher.publish(routingKey, event);
    }
}