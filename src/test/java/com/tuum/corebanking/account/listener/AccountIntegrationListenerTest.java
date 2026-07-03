package com.tuum.corebanking.account.listener;

import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.event.AccountCreatedEvent;
import com.tuum.corebanking.messaging.event.OperationType;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountIntegrationListenerTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private AccountIntegrationListener listener;

    @Test
    void handleAccountCreatedEventShouldPublishWithCorrectRoutingKey() {
        AccountCreatedEvent event = mock(AccountCreatedEvent.class);
        AccountResponse payload = mock(AccountResponse.class);

        when(payload.id()).thenReturn(UUID.randomUUID());
        when(event.payload()).thenReturn(payload);
        when(event.operationType()).thenReturn(OperationType.INSERT);

        listener.handleAccountCreatedEvent(event);

        verify(eventPublisher).publish("account.insert", event);
    }

    @Test
    void handleAccountCreatedEventShouldPublishWithDifferentRoutingKeyBasedOnOperation() {
        AccountCreatedEvent event = mock(AccountCreatedEvent.class);
        AccountResponse payload = mock(AccountResponse.class);

        when(payload.id()).thenReturn(UUID.randomUUID());
        when(event.payload()).thenReturn(payload);
        when(event.operationType()).thenReturn(OperationType.UPDATE);

        listener.handleAccountCreatedEvent(event);

        verify(eventPublisher).publish("account.update", event);
    }
}