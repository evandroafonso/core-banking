package com.tuum.corebanking.exception;

import java.math.BigDecimal;

public class InvalidTransactionAmountException extends BusinessException {
    public InvalidTransactionAmountException(String message) {
        super(message);
    }

    public InvalidTransactionAmountException(BigDecimal amount) {
        super("Amount must be greater than zero, received: %s".formatted(amount.toPlainString()));
    }
}