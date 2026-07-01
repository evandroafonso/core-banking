package com.tuum.corebanking.exception;

public class InvalidTransactionDirectionException extends BusinessException {
    public InvalidTransactionDirectionException(String message) {
        super(message);
    }
}