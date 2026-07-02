package com.tuum.corebanking.account.service;

import com.tuum.corebanking.account.converter.AccountConverter;
import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.event.AccountCreatedEvent;
import com.tuum.corebanking.account.mapper.AccountMapper;
import com.tuum.corebanking.account.model.Account;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.balance.service.BalanceService;
import com.tuum.corebanking.common.util.CurrencyParser;
import com.tuum.corebanking.exception.AccountNotFoundException;
import com.tuum.corebanking.messaging.event.OperationType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountMapper accountMapper;
    private final AccountConverter accountConverter;
    private final BalanceService balanceService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountService(
            AccountMapper accountMapper,
            AccountConverter accountConverter,
            BalanceService balanceService,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.accountMapper = accountMapper;
        this.accountConverter = accountConverter;
        this.balanceService = balanceService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        List<Currency> currencies = CurrencyParser.parseList(request.currencies());

        Account account = accountConverter.toEntity(request);
        account.initAuditFields();
        accountMapper.insert(account);

        List<BalanceResponse> balances = balanceService.create(currencies, account.getId());
        AccountResponse accountResponse = accountConverter.toResponse(account, balances);

        publishEvent(accountResponse);
        return accountResponse;
    }

    private void publishEvent(AccountResponse accountResponse) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                "AccountCreated",
                OperationType.INSERT,
                accountResponse
        );
        applicationEventPublisher.publishEvent(event);
    }

    public AccountResponse findById(UUID accountId) {
        Account account = accountMapper.findByBusinessId(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: %s".formatted(accountId)));
        List<BalanceResponse> balancesResponse = balanceService.findByAccountId(account.getId());

        return accountConverter.toResponse(account, balancesResponse);
    }

    public Long findAccountIdByBusinessId(UUID accountBusinessId) {
        return accountMapper.findAccountIdByBusinessId(accountBusinessId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: %s".formatted(accountBusinessId)));
    }
}