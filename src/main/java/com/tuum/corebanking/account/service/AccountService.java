package com.tuum.corebanking.account.service;

import com.tuum.corebanking.account.converter.AccountConverter;
import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.mapper.AccountMapper;
import com.tuum.corebanking.account.model.Account;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.balance.service.BalanceService;
import com.tuum.corebanking.exception.InvalidCurrencyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountMapper accountMapper;
    private final AccountConverter accountConverter;
    private final BalanceService balanceService;
//    private final EventPublisher eventPublisher;

    public AccountService(AccountMapper accountMapper, AccountConverter accountConverter, BalanceService balanceService) {
        this.accountMapper = accountMapper;
        this.accountConverter = accountConverter;
        this.balanceService = balanceService;
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        List<Currency> currencies = validateAndConvertCurrencies(request.currencies());

        Account account = accountConverter.toEntity(request);
        account.setBusinessId(UUID.randomUUID());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        accountMapper.insert(account);
        List<BalanceResponse> balances = balanceService.create(currencies, request, account.getId());

//        eventPublisher.publishAccountCreated(account, balances);

        return accountConverter.toResponse(account, balances);
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