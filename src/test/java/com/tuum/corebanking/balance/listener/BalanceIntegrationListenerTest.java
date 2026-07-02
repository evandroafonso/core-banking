package com.tuum.corebanking.balance.listener;

import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.event.BalanceCreatedEvent;
import com.tuum.corebanking.balance.event.BalanceUpdateEvent;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.messaging.event.OperationType;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BalanceIntegrationListenerTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private BalanceIntegrationListener listener;

    @Test
    void handleBalanceCreatedEventShouldPublishWithCorrectRoutingKeyForInsertOperation() {
        BalanceResponse response = new BalanceResponse(BigDecimal.TEN, Currency.EUR);
        BalanceCreatedEvent event = new BalanceCreatedEvent(
                "BalanceCreated",
                OperationType.INSERT,
                response
        );

        listener.handleBalanceCreatedEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BalanceCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BalanceCreatedEvent.class);

        verify(eventPublisher).publish(routingKeyCaptor.capture(), eventCaptor.capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("balance.insert");
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    void handleBalanceCreatedEventShouldPublishWithCorrectRoutingKeyForUpdateOperation() {
        BalanceResponse response = new BalanceResponse(BigDecimal.ONE, Currency.USD);
        BalanceCreatedEvent event = new BalanceCreatedEvent(
                "BalanceCreated",
                OperationType.UPDATE,
                response
        );

        listener.handleBalanceCreatedEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher).publish(routingKeyCaptor.capture(), ArgumentCaptor.forClass(BalanceCreatedEvent.class).capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("balance.update");
    }

    @Test
    void handleBalanceUpdateEventShouldPublishWithCorrectRoutingKeyForInsertOperation() {
        BalanceResponse response = new BalanceResponse(new BigDecimal("50.00"), Currency.EUR);
        BalanceUpdateEvent event = new BalanceUpdateEvent(
                "BalanceUpdated",
                OperationType.INSERT,
                response
        );

        listener.handleBalanceUpdateEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BalanceUpdateEvent> eventCaptor = ArgumentCaptor.forClass(BalanceUpdateEvent.class);

        verify(eventPublisher).publish(routingKeyCaptor.capture(), eventCaptor.capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("balance.insert");
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    void handleBalanceUpdateEventShouldPublishWithCorrectRoutingKeyForUpdateOperation() {
        BalanceResponse response = new BalanceResponse(BigDecimal.ZERO, Currency.USD);
        BalanceUpdateEvent event = new BalanceUpdateEvent(
                "BalanceUpdated",
                OperationType.UPDATE,
                response
        );

        listener.handleBalanceUpdateEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher).publish(routingKeyCaptor.capture(), ArgumentCaptor.forClass(BalanceUpdateEvent.class).capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("balance.update");
    }

    @Test
    void handleBalanceCreatedEventShouldConvertOperationTypeToLowerCase() {
        BalanceResponse response = new BalanceResponse(BigDecimal.ONE, Currency.USD);
        BalanceCreatedEvent event = new BalanceCreatedEvent(
                "BalanceCreated",
                OperationType.INSERT,
                response
        );

        listener.handleBalanceCreatedEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher).publish(routingKeyCaptor.capture(), ArgumentCaptor.forClass(BalanceCreatedEvent.class).capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("balance.insert");
        assertThat(routingKeyCaptor.getValue()).doesNotContain("INSERT");
        assertThat(routingKeyCaptor.getValue()).doesNotContain("Insert");
    }

    @Test
    void handleBalanceUpdateEventShouldConvertOperationTypeToLowerCase() {
        BalanceResponse response = new BalanceResponse(BigDecimal.TEN, Currency.EUR);
        BalanceUpdateEvent event = new BalanceUpdateEvent(
                "BalanceUpdated",
                OperationType.UPDATE,
                response
        );

        listener.handleBalanceUpdateEvent(event);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher).publish(routingKeyCaptor.capture(), ArgumentCaptor.forClass(BalanceUpdateEvent.class).capture());

        assertThat(routingKeyCaptor.getValue()).isEqualTo("balance.update");
        assertThat(routingKeyCaptor.getValue()).doesNotContain("UPDATE");
        assertThat(routingKeyCaptor.getValue()).doesNotContain("Update");
    }

    @Test
    void handleBalanceCreatedEventShouldPreserveEventPayload() {
        BalanceResponse response = new BalanceResponse(new BigDecimal("123.45"), Currency.EUR);
        BalanceCreatedEvent event = new BalanceCreatedEvent(
                "BalanceCreated",
                OperationType.INSERT,
                response
        );

        listener.handleBalanceCreatedEvent(event);

        ArgumentCaptor<BalanceCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BalanceCreatedEvent.class);
        verify(eventPublisher).publish(ArgumentCaptor.forClass(String.class).capture(), eventCaptor.capture());

        BalanceCreatedEvent captured = eventCaptor.getValue();
        assertThat(captured.payload()).isEqualTo(response);
        assertThat(captured.payload().balance()).isEqualTo(new BigDecimal("123.45"));
        assertThat(captured.payload().currency()).isEqualTo(Currency.EUR);
    }

    @Test
    void handleBalanceUpdateEventShouldPreserveEventPayload() {
        BalanceResponse response = new BalanceResponse(BigDecimal.ZERO, Currency.USD);
        BalanceUpdateEvent event = new BalanceUpdateEvent(
                "BalanceUpdated",
                OperationType.UPDATE,
                response
        );

        listener.handleBalanceUpdateEvent(event);

        ArgumentCaptor<BalanceUpdateEvent> eventCaptor = ArgumentCaptor.forClass(BalanceUpdateEvent.class);
        verify(eventPublisher).publish(ArgumentCaptor.forClass(String.class).capture(), eventCaptor.capture());

        BalanceUpdateEvent captured = eventCaptor.getValue();
        assertThat(captured.payload()).isEqualTo(response);
        assertThat(captured.payload().balance()).isEqualTo(BigDecimal.ZERO);
        assertThat(captured.payload().currency()).isEqualTo(Currency.USD);
    }

    @Test
    void handleBalanceCreatedEventShouldNotModifyOriginalEvent() {
        BalanceResponse response = new BalanceResponse(BigDecimal.ONE, Currency.EUR);
        BalanceCreatedEvent original = new BalanceCreatedEvent(
                "BalanceCreated",
                OperationType.INSERT,
                response
        );

        listener.handleBalanceCreatedEvent(original);

        assertThat(original.eventName()).isEqualTo("BalanceCreated");
        assertThat(original.operationType()).isEqualTo(OperationType.INSERT);
        assertThat(original.payload()).isEqualTo(response);
        assertThat(original.occurredAt()).isNotNull();
    }

    @Test
    void handleBalanceUpdateEventShouldNotModifyOriginalEvent() {
        BalanceResponse response = new BalanceResponse(BigDecimal.TEN, Currency.USD);
        BalanceUpdateEvent original = new BalanceUpdateEvent(
                "BalanceUpdated",
                OperationType.UPDATE,
                response
        );

        listener.handleBalanceUpdateEvent(original);

        assertThat(original.eventName()).isEqualTo("BalanceUpdated");
        assertThat(original.operationType()).isEqualTo(OperationType.UPDATE);
        assertThat(original.payload()).isEqualTo(response);
        assertThat(original.occurredAt()).isNotNull();
    }
}