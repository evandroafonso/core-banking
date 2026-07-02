package com.tuum.corebanking.exception;

import java.util.UUID;

public class AccountNotFoundException extends BusinessException {
    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(Long id) {
        super("Account not found with id: %s".formatted(id));
    }

    public AccountNotFoundException(UUID businessId) {
        super("Account not found with id: %s".formatted(businessId));
    }

}