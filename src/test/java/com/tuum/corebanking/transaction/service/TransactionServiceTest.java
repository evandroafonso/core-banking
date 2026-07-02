package com.tuum.corebanking.transaction.service;

import com.tuum.corebanking.account.service.AccountService;
import com.tuum.corebanking.balance.mapper.BalanceMapper;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionConverter transactionConverter;

    @Mock
    private AccountService accountService;

    @Mock
    private BalanceMapper balanceMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createShouldThrowInvalidTransactionAmountExceptionWhenAmountIsZero() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.ZERO, "EUR", "IN", "Description");

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(InvalidTransactionAmountException.class);

        verifyNoInteractions(accountService, balanceMapper, transactionMapper, transactionConverter);
    }

    @Test
    void createShouldThrowInvalidTransactionAmountExceptionWhenAmountIsNegative() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(new BigDecimal("-10.00"), "EUR", "IN", "Description");

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(InvalidTransactionAmountException.class);

        verifyNoInteractions(accountService, balanceMapper, transactionMapper, transactionConverter);
    }

    @Test
    void createShouldThrowAccountNotFoundExceptionWhenBalanceDoesNotExist() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.TEN, "EUR", "IN", "Description");
        Long accountId = 1L;

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, Currency.EUR)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("No balance found for account");

        verifyNoInteractions(transactionMapper, transactionConverter);
    }

    @Test
    void createShouldThrowInsufficientFundsExceptionWhenDirectionIsOutAndBalanceIsLow() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.TEN, "EUR", "OUT", "Description");
        Long accountId = 1L;
        Balance balance = Balance.builder()
                .id(10L)
                .accountId(accountId)
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.ONE)
                .build();

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, Currency.EUR)).thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(InsufficientFundsException.class);

        verifyNoInteractions(transactionMapper, transactionConverter);
    }

    @Test
    void createShouldThrowIllegalStateExceptionWhenUpdateBalanceAffectsZeroRows() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.TEN, "EUR", "IN", "Description");
        Long accountId = 1L;
        Balance balance = Balance.builder()
                .id(10L)
                .accountId(accountId)
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.ZERO)
                .build();

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, Currency.EUR)).thenReturn(Optional.of(balance));
        when(balanceMapper.updateAvailableAmount(10L, BigDecimal.TEN)).thenReturn(0);

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to update balance, no rows affected for id: 10");

        verifyNoInteractions(transactionMapper, transactionConverter);
    }

    @Test
    void createShouldSucceedWhenDirectionIsIn() {
        UUID accountBusinessId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.TEN, "EUR", "IN", "Description");
        Long accountId = 1L;
        Balance balance = Balance.builder()
                .id(10L)
                .accountId(accountId)
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.ZERO)
                .build();
        Transaction transaction = new Transaction();
        TransactionResponse expectedResponse = new TransactionResponse(
                accountBusinessId, transactionId, BigDecimal.TEN, Currency.EUR, Direction.IN, "Description", BigDecimal.TEN
        );

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, Currency.EUR)).thenReturn(Optional.of(balance));
        when(balanceMapper.updateAvailableAmount(10L, BigDecimal.TEN)).thenReturn(1);
        when(transactionConverter.toEntity(request, accountId, BigDecimal.TEN, Currency.EUR, Direction.IN)).thenReturn(transaction);
        when(transactionConverter.toResponse(transaction, accountBusinessId)).thenReturn(expectedResponse);

        TransactionResponse actualResponse = transactionService.create(accountBusinessId, request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(transactionMapper).insert(transaction);
    }

    @Test
    void createShouldSucceedWhenDirectionIsOutAndFundsAreSufficient() {
        UUID accountBusinessId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.TEN, "EUR", "OUT", "Description");
        Long accountId = 1L;
        Balance balance = Balance.builder()
                .id(10L)
                .accountId(accountId)
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.valueOf(50.00))
                .build();
        Transaction transaction = new Transaction();
        BigDecimal expectedBalanceAfter = BigDecimal.valueOf(40.00);
        TransactionResponse expectedResponse = new TransactionResponse(
                accountBusinessId, transactionId, BigDecimal.TEN, Currency.EUR, Direction.OUT, "Description", expectedBalanceAfter
        );

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, Currency.EUR)).thenReturn(Optional.of(balance));
        when(balanceMapper.updateAvailableAmount(10L, expectedBalanceAfter)).thenReturn(1);
        when(transactionConverter.toEntity(request, accountId, expectedBalanceAfter, Currency.EUR, Direction.OUT)).thenReturn(transaction);
        when(transactionConverter.toResponse(transaction, accountBusinessId)).thenReturn(expectedResponse);

        TransactionResponse actualResponse = transactionService.create(accountBusinessId, request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(transactionMapper).insert(transaction);
    }

    @Test
    void findByAccountIdShouldThrowAccountNotFoundExceptionWhenNoTransactionsExist() {
        UUID accountBusinessId = UUID.randomUUID();
        Long accountId = 1L;

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(transactionMapper.findByAccountId(accountId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> transactionService.findByAccountId(accountBusinessId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(String.valueOf(accountId));

        verifyNoInteractions(transactionConverter);
    }

    @Test
    void findByAccountIdShouldReturnTransactionsWhenTheyExist() {
        UUID accountBusinessId = UUID.randomUUID();
        Long accountId = 1L;
        Transaction transaction = new Transaction();
        List<Transaction> transactions = List.of(transaction);
        List<TransactionResponse> expectedResponses = List.of(new TransactionResponse(
                accountBusinessId, UUID.randomUUID(), BigDecimal.TEN, Currency.EUR, Direction.IN, "Description", BigDecimal.TEN
        ));

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(transactionMapper.findByAccountId(accountId)).thenReturn(transactions);
        when(transactionConverter.toResponses(transactions, accountBusinessId)).thenReturn(expectedResponses);

        List<TransactionResponse> actualResponses = transactionService.findByAccountId(accountBusinessId);

        assertThat(actualResponses).isEqualTo(expectedResponses);
    }
}