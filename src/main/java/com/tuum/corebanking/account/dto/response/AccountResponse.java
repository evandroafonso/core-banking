package com.tuum.corebanking.account.dto.response;

import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record AccountResponse(
        UUID id,
        UUID customerId,
        List<BalanceResponse> balances
) {
}