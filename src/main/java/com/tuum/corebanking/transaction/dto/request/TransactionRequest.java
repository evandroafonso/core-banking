package com.tuum.corebanking.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotBlank(message = "Direction is required")
        String direction,

        @NotBlank(message = "Description is required")
        String description
) {
}