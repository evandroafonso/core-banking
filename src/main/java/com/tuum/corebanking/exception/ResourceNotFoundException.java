package com.tuum.corebanking.exception;

import java.util.UUID;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(UUID businessId) {
        super("Resource not found with id: %s".formatted(businessId));
    }

}