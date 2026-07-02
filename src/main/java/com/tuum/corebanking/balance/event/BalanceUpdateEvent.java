package com.tuum.corebanking.balance.event;

import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.messaging.event.OperationType;

import java.time.LocalDateTime;

public record BalanceUpdateEvent(
        String eventName,
        OperationType operationType,
        BalanceResponse payload,
        LocalDateTime occurredAt
) {
    public BalanceUpdateEvent(String eventName, OperationType operationType, BalanceResponse payload) {
        this(eventName, operationType, payload, LocalDateTime.now());
    }
}
