package com.tuum.corebanking.account.service;

import com.tuum.corebanking.account.converter.AccountConverter;
import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.mapper.AccountMapper;
import com.tuum.corebanking.account.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountMapper accountMapper;
    //    private final BalanceMapper balanceMapper;
    private final AccountConverter accountConverter;
//    private final EventPublisher eventPublisher;

    public AccountService(AccountMapper accountMapper, AccountConverter accountConverter) {
        this.accountMapper = accountMapper;
        this.accountConverter = accountConverter;
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        Account account = accountConverter.toEntity(request);
        account.setBusinessId(UUID.randomUUID());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        accountMapper.insert(account);

//        List<Balance> balances = accountConverter.toBalances(request, account.getId());
//        balances.forEach(balanceMapper::insert);
//
//        eventPublisher.publishAccountCreated(account, balances);

        return accountConverter.toResponse(account);
    }
}