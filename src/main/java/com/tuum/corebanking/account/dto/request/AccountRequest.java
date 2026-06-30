package com.tuum.corebanking.account.dto.request;

import com.tuum.corebanking.account.model.Currency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AccountRequest(
        @NotNull UUID customerId,
        @NotNull String country,
        @NotEmpty List<Currency> currencies
) {
}