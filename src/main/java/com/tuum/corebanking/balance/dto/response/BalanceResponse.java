package com.tuum.corebanking.balance.dto.response;

import com.tuum.corebanking.balance.model.Currency;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BalanceResponse(
        BigDecimal balance,
        Currency currency
) {
}