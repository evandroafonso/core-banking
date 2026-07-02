package com.tuum.corebanking.balance.converter;

import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BalanceConverter {

    public List<Balance> toEntities(List<Currency> currencies, Long accountId) {
        LocalDateTime now = LocalDateTime.now();

        return currencies.stream()
                .map(currency -> Balance.builder()
                        .businessId(UUID.randomUUID())
                        .accountId(accountId)
                        .currency(currency)
                        .availableAmount(BigDecimal.ZERO)
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .collect(Collectors.toList());
    }

    public List<BalanceResponse> toResponses(List<Balance> balances) {
        return balances.stream().map(
                balance -> BalanceResponse.builder()
                        .balance(balance.getAvailableAmount().stripTrailingZeros())
                        .currency(balance.getCurrency())
                        .build()
        ).collect(Collectors.toList());
    }

}
