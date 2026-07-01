package com.tuum.corebanking.exception;

public class InvalidTransactionAmountException extends BusinessException {
    public InvalidTransactionAmountException(String message) {
        super(message);
    }
}