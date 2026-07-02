package com.tuum.corebanking.transaction.event;

import com.tuum.corebanking.messaging.event.OperationType;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;

import java.time.LocalDateTime;

public record TransactionEvent(
        String eventName,
        OperationType operationType,
        TransactionResponse payload,
        LocalDateTime occurredAt
) {
    public TransactionEvent(String eventName, OperationType operationType, TransactionResponse payload) {
        this(eventName, operationType, payload, LocalDateTime.now());
    }
}