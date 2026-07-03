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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;
    private final AccountConverter accountConverter;
    private final BalanceService balanceService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AccountResponse create(AccountRequest request) {
        log.info("Creating account with customer ID: {}, country: {}, currencies: {}", 
                request.customerId(), request.country(), request.currencies());
        
        List<Currency> currencies = CurrencyParser.parseList(request.currencies());

        Account account = accountConverter.toEntity(request);
        account.initAuditFields();
        accountMapper.insert(account);

        List<BalanceResponse> balances = balanceService.create(currencies, account.getId());
        AccountResponse accountResponse = accountConverter.toResponse(account, balances);

        publishEvent(accountResponse);
        log.info("Account created successfully with ID: {}, business ID: {}", account.getId(), account.getBusinessId());
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
        log.debug("Finding account by business ID: {}", accountId);
        Account account = accountMapper.findByBusinessId(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: %s".formatted(accountId)));
        List<BalanceResponse> balancesResponse = balanceService.findByAccountId(account.getId());

        log.debug("Account found: {}", account.getBusinessId());
        return accountConverter.toResponse(account, balancesResponse);
    }

    @Cacheable(cacheNames = "accountIds", key = "#accountBusinessId")
    public Long findAccountIdByBusinessId(UUID accountBusinessId) {
        log.debug("Finding account ID by business ID: {}", accountBusinessId);
        Long accountId = accountMapper.findAccountIdByBusinessId(accountBusinessId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: %s".formatted(accountBusinessId)));
        log.debug("Account ID found: {} for business ID: {}", accountId, accountBusinessId);
        return accountId;
    }
}