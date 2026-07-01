package com.tuum.corebanking.account.converter;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.model.Account;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountConverter {

    public Account toEntity(AccountRequest request) {
        return Account.builder()
                .customerId(request.customerId())
                .country(request.country())
                .build();
    }

    public AccountResponse toResponse(Account account, List<BalanceResponse> balancesResponse) {
        return AccountResponse.builder()
                .id(account.getBusinessId())
                .customerId(account.getCustomerId())
                .balances(balancesResponse)
                .build();
    }

}