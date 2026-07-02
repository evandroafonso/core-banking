package com.tuum.corebanking.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BusinessException {

    public InsufficientFundsException(BigDecimal attemptedAmount) {
        super("Insufficient funds for the requested amount: %s".formatted(attemptedAmount));
    }

    public InsufficientFundsException(BigDecimal attemptedAmount, BigDecimal currentBalance) {
        super("Insufficient funds. Attempted to withdraw %s, but current balance is %s"
                .formatted(attemptedAmount, currentBalance));
    }
}