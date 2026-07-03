package com.tuum.corebanking.transaction.service;

import com.tuum.corebanking.account.service.AccountService;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.balance.service.BalanceService;
import com.tuum.corebanking.common.dto.PageResponse;
import com.tuum.corebanking.exception.InsufficientFundsException;
import com.tuum.corebanking.exception.InvalidTransactionAmountException;
import com.tuum.corebanking.exception.ResourceNotFoundException;
import com.tuum.corebanking.messaging.event.OperationType;
import com.tuum.corebanking.transaction.converter.TransactionConverter;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.event.TransactionEvent;
import com.tuum.corebanking.transaction.mapper.TransactionMapper;
import com.tuum.corebanking.transaction.model.Direction;
import com.tuum.corebanking.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
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
    private BalanceService balanceService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createShouldThrowInvalidTransactionAmountExceptionWhenAmountIsZero() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.ZERO, "EUR", "IN", "Description");

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(InvalidTransactionAmountException.class);

        verifyNoInteractions(accountService, balanceService, transactionMapper, transactionConverter, applicationEventPublisher);
    }

    @Test
    void createShouldThrowInvalidTransactionAmountExceptionWhenAmountIsNegative() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(new BigDecimal("-10.00"), "EUR", "IN", "Description");

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(InvalidTransactionAmountException.class);

        verifyNoInteractions(accountService, balanceService, transactionMapper, transactionConverter, applicationEventPublisher);
    }

    @Test
    void createShouldThrowAccountNotFoundExceptionWhenBalanceDoesNotExist() {
        UUID accountBusinessId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(BigDecimal.TEN, "EUR", "IN", "Description");
        Long accountId = 1L;

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(balanceService.findBalanceWithLock(accountId, accountBusinessId, Currency.EUR))
                .thenThrow(new ResourceNotFoundException("No balance found for account"));

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No balance found for account");

        verify(accountService).findAccountIdByBusinessId(accountBusinessId);
        verify(balanceService).findBalanceWithLock(accountId, accountBusinessId, Currency.EUR);
        verifyNoInteractions(transactionMapper, transactionConverter, applicationEventPublisher);
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
        when(balanceService.findBalanceWithLock(accountId, accountBusinessId, Currency.EUR)).thenReturn(balance);

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(InsufficientFundsException.class);

        verify(accountService).findAccountIdByBusinessId(accountBusinessId);
        verify(balanceService).findBalanceWithLock(accountId, accountBusinessId, Currency.EUR);
        verifyNoInteractions(transactionMapper, transactionConverter, applicationEventPublisher);
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
        when(balanceService.findBalanceWithLock(accountId, accountBusinessId, Currency.EUR)).thenReturn(balance);
        doThrow(new IllegalStateException("Failed to update balance, no rows affected for id: 10"))
                .when(balanceService).update(balance, BigDecimal.TEN);

        assertThatThrownBy(() -> transactionService.create(accountBusinessId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to update balance, no rows affected for id: 10");

        verify(accountService).findAccountIdByBusinessId(accountBusinessId);
        verify(balanceService).findBalanceWithLock(accountId, accountBusinessId, Currency.EUR);
        verify(balanceService).update(balance, BigDecimal.TEN);
        verifyNoInteractions(transactionMapper, transactionConverter, applicationEventPublisher);
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
        when(balanceService.findBalanceWithLock(accountId, accountBusinessId, Currency.EUR)).thenReturn(balance);
        when(transactionConverter.toEntity(request, accountId, BigDecimal.TEN, Currency.EUR, Direction.IN)).thenReturn(transaction);
        when(transactionConverter.toResponse(transaction, accountBusinessId)).thenReturn(expectedResponse);

        TransactionResponse actualResponse = transactionService.create(accountBusinessId, request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(transactionMapper).insert(transaction);
        verify(balanceService).update(balance, BigDecimal.TEN);

        ArgumentCaptor<TransactionEvent> eventCaptor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        TransactionEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.eventName()).isEqualTo("TransactionCreated");
        assertThat(capturedEvent.operationType()).isEqualTo(OperationType.INSERT);
        assertThat(capturedEvent.payload()).isEqualTo(expectedResponse);
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
        when(balanceService.findBalanceWithLock(accountId, accountBusinessId, Currency.EUR)).thenReturn(balance);
        when(transactionConverter.toEntity(request, accountId, expectedBalanceAfter, Currency.EUR, Direction.OUT)).thenReturn(transaction);
        when(transactionConverter.toResponse(transaction, accountBusinessId)).thenReturn(expectedResponse);

        TransactionResponse actualResponse = transactionService.create(accountBusinessId, request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(transactionMapper).insert(transaction);
        verify(balanceService).update(balance, expectedBalanceAfter);

        ArgumentCaptor<TransactionEvent> eventCaptor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        TransactionEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.eventName()).isEqualTo("TransactionCreated");
        assertThat(capturedEvent.operationType()).isEqualTo(OperationType.INSERT);
        assertThat(capturedEvent.payload()).isEqualTo(expectedResponse);
    }

    @Test
    void findByAccountIdShouldThrowAccountNotFoundExceptionWhenNoTransactionsExist() {
        UUID accountBusinessId = UUID.randomUUID();
        Long accountId = 1L;

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(transactionMapper.findByAccountId(accountId, 0, 10)).thenReturn(Collections.emptyList());
        when(transactionMapper.countByAccountId(accountId)).thenReturn(0L);

        assertThatThrownBy(() -> transactionService.findByAccountId(accountBusinessId, 0, 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(accountBusinessId));

        verify(accountService).findAccountIdByBusinessId(accountBusinessId);
        verify(transactionMapper).findByAccountId(accountId, 0, 10);
        verify(transactionMapper).countByAccountId(accountId);
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
        when(transactionMapper.findByAccountId(accountId, 0, 10)).thenReturn(transactions);
        when(transactionMapper.countByAccountId(accountId)).thenReturn(1L);
        when(transactionConverter.toResponses(transactions, accountBusinessId)).thenReturn(expectedResponses);

        PageResponse<TransactionResponse> actualResponse = transactionService.findByAccountId(accountBusinessId, 0, 10);

        assertThat(actualResponse.data()).isEqualTo(expectedResponses);
        assertThat(actualResponse.page()).isEqualTo(0);
        assertThat(actualResponse.size()).isEqualTo(10);
        assertThat(actualResponse.totalElements()).isEqualTo(1);
        assertThat(actualResponse.totalPages()).isEqualTo(1);
        verify(accountService).findAccountIdByBusinessId(accountBusinessId);
        verify(transactionMapper).findByAccountId(accountId, 0, 10);
        verify(transactionMapper).countByAccountId(accountId);
        verify(transactionConverter).toResponses(transactions, accountBusinessId);
    }

    @Test
    void findByAccountIdShouldReturnEmptyPageWhenNoTransactionsOnSecondPage() {
        UUID accountBusinessId = UUID.randomUUID();
        Long accountId = 1L;

        when(accountService.findAccountIdByBusinessId(accountBusinessId)).thenReturn(accountId);
        when(transactionMapper.findByAccountId(accountId, 10, 10)).thenReturn(Collections.emptyList());
        when(transactionMapper.countByAccountId(accountId)).thenReturn(5L);
        when(transactionConverter.toResponses(Collections.emptyList(), accountBusinessId)).thenReturn(Collections.emptyList());

        PageResponse<TransactionResponse> actualResponse = transactionService.findByAccountId(accountBusinessId, 1, 10);

        assertThat(actualResponse.data()).isEmpty();
        assertThat(actualResponse.page()).isEqualTo(1);
        assertThat(actualResponse.size()).isEqualTo(10);
        assertThat(actualResponse.totalElements()).isEqualTo(5);
        assertThat(actualResponse.totalPages()).isEqualTo(1);
        verify(accountService).findAccountIdByBusinessId(accountBusinessId);
        verify(transactionMapper).findByAccountId(accountId, 10, 10);
        verify(transactionMapper).countByAccountId(accountId);
        verify(transactionConverter).toResponses(Collections.emptyList(), accountBusinessId);
    }
}