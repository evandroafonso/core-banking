package com.tuum.corebanking.transaction.dto.response;

import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.transaction.model.Direction;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionResponse(
        UUID accountId,
        UUID transactionId,
        BigDecimal amount,
        Currency currency,
        Direction direction,
        String description,
        BigDecimal balanceAfter
) {
}