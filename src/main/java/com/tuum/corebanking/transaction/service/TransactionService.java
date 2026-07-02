package com.tuum.corebanking.transaction.service;

import com.tuum.corebanking.account.service.AccountService;
import com.tuum.corebanking.balance.mapper.BalanceMapper;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.common.util.CurrencyParser;
import com.tuum.corebanking.common.util.DirectionParser;
import com.tuum.corebanking.exception.AccountNotFoundException;
import com.tuum.corebanking.exception.InsufficientFundsException;
import com.tuum.corebanking.exception.InvalidTransactionAmountException;
import com.tuum.corebanking.messaging.publisher.EventPublisher;
import com.tuum.corebanking.transaction.converter.TransactionConverter;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.mapper.TransactionMapper;
import com.tuum.corebanking.transaction.model.Direction;
import com.tuum.corebanking.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final TransactionConverter transactionConverter;
    private final AccountService accountService;
    private final BalanceMapper balanceMapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public TransactionResponse create(UUID accountBusinessId, TransactionRequest request) {
        validateAmount(request.amount());

        Currency currency = CurrencyParser.parse(request.currency());
        Direction direction = DirectionParser.parse(request.direction());

        Long accountId = accountService.findAccountIdByBusinessId(accountBusinessId);
        Balance balance = findBalanceWithLock(accountId, accountBusinessId, currency);

        BigDecimal balanceAfter = calculateNewBalance(balance, request.amount(), direction);

        updateBalance(balance.getId(), balanceAfter);
        Transaction transaction = save(request, balance.getAccountId(), balanceAfter, currency, direction);

        TransactionResponse response = transactionConverter.toResponse(transaction, accountBusinessId);
        publishEvent(response);

        return response;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionAmountException(amount);
        }
    }

    private Balance findBalanceWithLock(Long accountId, UUID accountBusinessId, Currency currency) {
        return balanceMapper
                .findByAccountIdAndCurrencyForUpdate(accountId, currency)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No balance found for account %s with currency %s"
                                .formatted(accountBusinessId, currency)
                ));
    }

    private BigDecimal calculateNewBalance(Balance balance, BigDecimal amount, Direction direction) {
        BigDecimal newBalance = direction == Direction.IN
                ? balance.getAvailableAmount().add(amount)
                : balance.getAvailableAmount().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(amount, balance.getAvailableAmount());
        }

        return newBalance;
    }

    private void updateBalance(Long balanceId, BigDecimal balanceAfter) {
        int rowsAffected = balanceMapper.updateAvailableAmount(balanceId, balanceAfter);
        if (rowsAffected == 0) {
            throw new IllegalStateException("Failed to update balance, no rows affected for id: %s".formatted(balanceId));
        }
    }

    private Transaction save(TransactionRequest request, Long accountId, BigDecimal balanceAfter,
                             Currency currency, Direction direction) {
        Transaction transaction = transactionConverter.toEntity(request, accountId, balanceAfter, currency, direction);
        transactionMapper.insert(transaction);
        return transaction;
    }

    private void publishEvent(TransactionResponse response) {
        // eventPublisher.publish(TransactionEvent.created(response));
    }

    public List<TransactionResponse> findByAccountId(UUID accountBusinessId) {
        Long accountId = accountService.findAccountIdByBusinessId(accountBusinessId);
        List<Transaction> transactions = transactionMapper.findByAccountId(accountId);
        if (transactions.isEmpty()) {
            throw new AccountNotFoundException(String.valueOf(accountId));
        }
        return transactionConverter.toResponses(transactions, accountBusinessId);
    }
}