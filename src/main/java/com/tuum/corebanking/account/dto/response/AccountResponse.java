package com.tuum.corebanking.account.dto.response;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId
        //  List<BalanceResponse> balances
) {
}