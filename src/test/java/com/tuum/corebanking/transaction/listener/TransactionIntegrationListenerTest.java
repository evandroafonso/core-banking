package com.tuum.corebanking.transaction.listener;

import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.messaging.event.OperationType;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.event.TransactionEvent;
import com.tuum.corebanking.transaction.model.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionIntegrationListenerTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TransactionIntegrationListener transactionIntegrationListener;

    @Test
    void handleTransactionCreatedEventShouldPublishWithCorrectRoutingKeyForInsertOperation() {
        TransactionResponse transactionResponse = new TransactionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                Direction.IN,
                "Test transaction",
                BigDecimal.valueOf(100)
        );

        TransactionEvent event = new TransactionEvent(
                "TransactionCreated",
                OperationType.INSERT,
                transactionResponse
        );

        transactionIntegrationListener.handleTransactionCreatedEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TransactionEvent> eventCaptor = ArgumentCaptor.forClass(TransactionEvent.class);

        verify(eventPublisher).publish(routingKeyCaptor.capture(), eventCaptor.capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("transaction.insert");
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    void handleTransactionCreatedEventShouldPublishWithCorrectRoutingKeyForUpdateOperation() {
        TransactionResponse transactionResponse = new TransactionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ONE,
                Currency.USD,
                Direction.OUT,
                "Update transaction",
                BigDecimal.valueOf(50)
        );

        TransactionEvent event = new TransactionEvent(
                "TransactionUpdated",
                OperationType.UPDATE,
                transactionResponse
        );

        transactionIntegrationListener.handleTransactionCreatedEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TransactionEvent> eventCaptor = ArgumentCaptor.forClass(TransactionEvent.class);

        verify(eventPublisher).publish(routingKeyCaptor.capture(), eventCaptor.capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("transaction.update");
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    void handleTransactionCreatedEventShouldFormatRoutingKeyWithPrefixTransaction() {
        TransactionResponse transactionResponse = new TransactionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                Direction.IN,
                "Test",
                BigDecimal.TEN
        );

        TransactionEvent event = new TransactionEvent(
                "TransactionCreated",
                OperationType.INSERT,
                transactionResponse
        );

        transactionIntegrationListener.handleTransactionCreatedEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(eventPublisher).publish(routingKeyCaptor.capture(), ArgumentCaptor.forClass(TransactionEvent.class).capture());

        assertThat(routingKeyCaptor.getValue()).startsWith("transaction.");
    }

    @Test
    void handleTransactionCreatedEventShouldConvertOperationTypeToLowerCase() {
        TransactionResponse transactionResponse = new TransactionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                Direction.IN,
                "Test",
                BigDecimal.TEN
        );

        TransactionEvent event = new TransactionEvent(
                "TransactionCreated",
                OperationType.INSERT,
                transactionResponse
        );

        transactionIntegrationListener.handleTransactionCreatedEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(eventPublisher).publish(routingKeyCaptor.capture(), ArgumentCaptor.forClass(TransactionEvent.class).capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("transaction.insert");
        assertThat(routingKeyCaptor.getValue()).doesNotContain("INSERT");
        assertThat(routingKeyCaptor.getValue()).doesNotContain("Insert");
    }

    @Test
    void handleTransactionCreatedEventShouldPassCorrectEventToPublisher() {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransactionResponse transactionResponse = new TransactionResponse(
                accountId,
                transactionId,
                BigDecimal.TEN,
                Currency.EUR,
                Direction.IN,
                "Test transaction",
                BigDecimal.valueOf(100)
        );

        TransactionEvent event = new TransactionEvent(
                "TransactionCreated",
                OperationType.INSERT,
                transactionResponse
        );

        transactionIntegrationListener.handleTransactionCreatedEvent(event);

        ArgumentCaptor<TransactionEvent> eventCaptor = ArgumentCaptor.forClass(TransactionEvent.class);

        verify(eventPublisher).publish(ArgumentCaptor.forClass(String.class).capture(), eventCaptor.capture());

        TransactionEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isNotNull();
        assertThat(capturedEvent.eventName()).isEqualTo("TransactionCreated");
        assertThat(capturedEvent.operationType()).isEqualTo(OperationType.INSERT);
        assertThat(capturedEvent.payload()).isEqualTo(transactionResponse);
        assertThat(capturedEvent.payload().accountId()).isEqualTo(accountId);
        assertThat(capturedEvent.payload().transactionId()).isEqualTo(transactionId);
        assertThat(capturedEvent.payload().amount()).isEqualTo(BigDecimal.TEN);
        assertThat(capturedEvent.payload().currency()).isEqualTo(Currency.EUR);
        assertThat(capturedEvent.payload().direction()).isEqualTo(Direction.IN);
        assertThat(capturedEvent.payload().description()).isEqualTo("Test transaction");
        assertThat(capturedEvent.payload().balanceAfter()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void handleTransactionCreatedEventShouldNotModifyOriginalEvent() {
        TransactionResponse transactionResponse = new TransactionResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                Currency.EUR,
                Direction.IN,
                "Test transaction",
                BigDecimal.valueOf(100)
        );

        TransactionEvent event = new TransactionEvent(
                "TransactionCreated",
                OperationType.INSERT,
                transactionResponse
        );

        TransactionEvent originalEvent = new TransactionEvent(
                event.eventName(),
                event.operationType(),
                event.payload()
        );

        transactionIntegrationListener.handleTransactionCreatedEvent(event);

        assertThat(event.eventName()).isEqualTo(originalEvent.eventName());
        assertThat(event.operationType()).isEqualTo(originalEvent.operationType());
        assertThat(event.payload()).isEqualTo(originalEvent.payload());
    }
}