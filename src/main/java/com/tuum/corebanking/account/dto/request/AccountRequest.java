package com.tuum.corebanking.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

public record AccountRequest(

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotBlank(message = "Country is required")
        @Pattern(regexp = "[A-Z]{2}", message = "Country must be uppercase 2-letter ISO code (e.g. EE, US, GB)")
        String country,

        @NotEmpty(message = "At least one currency is required")
        List<String> currencies

) {
}