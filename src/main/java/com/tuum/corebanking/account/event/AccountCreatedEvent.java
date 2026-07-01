package com.tuum.corebanking.account.event;

import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.messaging.event.OperationType;

import java.time.LocalDateTime;

public record AccountCreatedEvent(
        String eventName,
        OperationType operationType,
        AccountResponse payload,
        LocalDateTime occurredAt
) {
    public AccountCreatedEvent(String eventName, OperationType operationType, AccountResponse payload) {
        this(eventName, operationType, payload, LocalDateTime.now());
    }
}