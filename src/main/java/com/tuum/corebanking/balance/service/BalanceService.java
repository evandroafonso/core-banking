package com.tuum.corebanking.balance.service;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.balance.converter.BalanceConverter;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.mapper.BalanceMapper;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BalanceService {

    private final BalanceMapper balanceMapper;
    private final BalanceConverter balanceConverter;

    public BalanceService(BalanceMapper balanceMapper, BalanceConverter balanceConverter) {
        this.balanceMapper = balanceMapper;
        this.balanceConverter = balanceConverter;
    }

    public List<BalanceResponse> create(List<Currency> currencies, AccountRequest accountRequest, Long accountId) {
        List<Balance> balances = balanceConverter.toEntities(currencies, accountId);
        balances.forEach(balanceMapper::insert);
        return balanceConverter.toResponses(balances);
    }

}
