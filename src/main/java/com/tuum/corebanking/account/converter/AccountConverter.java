package com.tuum.corebanking.account.converter;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountConverter {

    public Account toEntity(AccountRequest request) {
        return Account.builder()
                .customerId(request.customerId())
                .country(request.country())
                .build();
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getBusinessId(),
                account.getCustomerId()
//                balances.stream().map(this::toBalanceResponse).collect(Collectors.toList())
        );
    }

}