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
import com.tuum.corebanking.exception.InvalidCurrencyException;
import com.tuum.corebanking.messaging.event.OperationType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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
        List<Currency> currencies = validateAndConvertCurrencies(request.currencies());

        Account account = accountConverter.toEntity(request);
        account.initializeNewAccount();
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

    private List<Currency> validateAndConvertCurrencies(List<String> rawCurrencies) {
        return rawCurrencies.stream()
                .map(this::parseCurrency)
                .distinct()
                .toList();
    }

    private Currency parseCurrency(String raw) {
        try {
            return Currency.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyException(
                    "Invalid currency: '%s'. Accepted values: %s"
                            .formatted(raw, Arrays.toString(Currency.values()))
            );
        }
    }
}