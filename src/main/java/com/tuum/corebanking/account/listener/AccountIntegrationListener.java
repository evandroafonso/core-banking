package com.tuum.corebanking.account.listener;

import com.tuum.corebanking.account.event.AccountCreatedEvent;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class AccountIntegrationListener {

    private final EventPublisher eventPublisher;

    public AccountIntegrationListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        log.info("Handling AccountCreatedEvent for account ID: {}", event.payload().id());
        String routingKey = "account.%s".formatted(event.operationType().name().toLowerCase());
        eventPublisher.publish(routingKey, event);
        log.debug("AccountCreatedEvent published with routing key: {}", routingKey);
    }
}