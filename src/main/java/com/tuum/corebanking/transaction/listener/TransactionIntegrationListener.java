package com.tuum.corebanking.transaction.listener;

import com.tuum.corebanking.messaging.publisher.EventPublisher;
import com.tuum.corebanking.transaction.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class TransactionIntegrationListener {

    private final EventPublisher eventPublisher;

    public TransactionIntegrationListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventPublisherExecutor")
    public void handleTransactionCreatedEvent(TransactionEvent event) {
        log.info("Handling TransactionCreatedEvent for transaction ID: {}", event.payload().transactionId());
        String routingKey = "transaction.%s".formatted(event.operationType().name().toLowerCase());
        eventPublisher.publish(routingKey, event);
        log.debug("TransactionEvent published with routing key: {}", routingKey);
    }
}
