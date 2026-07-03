package com.tuum.corebanking.transaction.service;

import com.tuum.corebanking.account.service.AccountService;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.balance.service.BalanceService;
import com.tuum.corebanking.common.dto.PageResponse;
import com.tuum.corebanking.common.util.CurrencyParser;
import com.tuum.corebanking.common.util.DirectionParser;
import com.tuum.corebanking.exception.AccountNotFoundException;
import com.tuum.corebanking.exception.InsufficientFundsException;
import com.tuum.corebanking.exception.InvalidTransactionAmountException;
import com.tuum.corebanking.messaging.event.OperationType;
import com.tuum.corebanking.transaction.converter.TransactionConverter;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.event.TransactionEvent;
import com.tuum.corebanking.transaction.mapper.TransactionMapper;
import com.tuum.corebanking.transaction.model.Direction;
import com.tuum.corebanking.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final TransactionConverter transactionConverter;
    private final AccountService accountService;
    private final BalanceService balanceService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public TransactionResponse create(UUID accountBusinessId, TransactionRequest request) {
        log.info("Creating transaction for account: {}, amount: {}, currency: {}, direction: {}",
                accountBusinessId, request.amount(), request.currency(), request.direction());

        validateAmount(request.amount());

        Currency currency = CurrencyParser.parse(request.currency());
        Direction direction = DirectionParser.parse(request.direction());

        Long accountId = accountService.findAccountIdByBusinessId(accountBusinessId);
        Balance balance = findBalanceWithLock(accountId, accountBusinessId, currency);

        BigDecimal balanceAfter = calculateNewBalance(balance, request.amount(), direction);

        updateBalance(balance, balanceAfter);
        Transaction transaction = save(request, balance.getAccountId(), balanceAfter, currency, direction);

        TransactionResponse response = transactionConverter.toResponse(transaction, accountBusinessId);
        publishEvent(response);

        log.info("Transaction created successfully with ID: {} for account: {}", transaction.getBusinessId(), accountBusinessId);
        return response;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid transaction amount: {}", amount);
            throw new InvalidTransactionAmountException(amount);
        }
    }

    private Balance findBalanceWithLock(Long accountId, UUID accountBusinessId, Currency currency) {
        return balanceService.findBalanceWithLock(accountId, accountBusinessId, currency);
    }

    private BigDecimal calculateNewBalance(Balance balance, BigDecimal amount, Direction direction) {
        BigDecimal newBalance = direction == Direction.IN
                ? balance.getAvailableAmount().add(amount)
                : balance.getAvailableAmount().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Insufficient funds for account: {}. Available: {}, requested: {}",
                    balance.getAccountId(), balance.getAvailableAmount(), amount);
            throw new InsufficientFundsException(amount, balance.getAvailableAmount());
        }

        return newBalance;
    }

    private void updateBalance(Balance balance, BigDecimal balanceAfter) {
        balanceService.update(balance, balanceAfter);
    }

    private Transaction save(TransactionRequest request, Long accountId, BigDecimal balanceAfter,
                             Currency currency, Direction direction) {
        Transaction transaction = transactionConverter.toEntity(request, accountId, balanceAfter, currency, direction);
        transactionMapper.insert(transaction);
        return transaction;
    }

    private void publishEvent(TransactionResponse response) {
        TransactionEvent event = new TransactionEvent(
                "TransactionCreated",
                OperationType.INSERT,
                response
        );
        applicationEventPublisher.publishEvent(event);
    }

    public PageResponse<TransactionResponse> findByAccountId(UUID accountBusinessId, int page, int size) {
        log.debug("Finding transactions for account: {}, page: {}, size: {}", accountBusinessId, page, size);
        Long accountId = accountService.findAccountIdByBusinessId(accountBusinessId);
        int offset = page * size;
        List<Transaction> transactions = transactionMapper.findByAccountId(accountId, offset, size);
        long totalElements = transactionMapper.countByAccountId(accountId);

        if (transactions.isEmpty() && totalElements == 0) {
            log.warn("No transactions found for account ID: {}", accountId);
            throw new AccountNotFoundException(String.valueOf(accountId));
        }

        List<TransactionResponse> responses = transactionConverter.toResponses(transactions, accountBusinessId);
        log.info("Found {} transactions for account: {}", responses.size(), accountBusinessId);
        return new PageResponse<>(responses, page, size, totalElements);
    }
}