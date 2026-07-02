package com.tuum.corebanking.transaction.converter;

import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.model.Direction;
import com.tuum.corebanking.transaction.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionConverterTest {

    private final TransactionConverter transactionConverter = new TransactionConverter();

    @Test
    void toEntityShouldCreateTransactionWithAllFieldsCorrectly() {
        TransactionRequest request = new TransactionRequest(
                BigDecimal.TEN,
                "EUR",
                "IN",
                "Test transaction"
        );
        Long accountId = 1L;
        BigDecimal balanceAfter = BigDecimal.valueOf(100);
        Currency currency = Currency.EUR;
        Direction direction = Direction.IN;

        Transaction result = transactionConverter.toEntity(request, accountId, balanceAfter, currency, direction);

        assertThat(result).isNotNull();
        assertThat(result.getBusinessId()).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(result.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(result.getDirection()).isEqualTo(Direction.IN);
        assertThat(result.getDescription()).isEqualTo("Test transaction");
        assertThat(result.getBalanceAfter()).isEqualTo(balanceAfter);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toEntityShouldGenerateUniqueBusinessIdForEachTransaction() {
        TransactionRequest request = new TransactionRequest(
                BigDecimal.ONE,
                "USD",
                "OUT",
                "Description"
        );
        Long accountId = 1L;
        BigDecimal balanceAfter = BigDecimal.ZERO;

        Transaction transaction1 = transactionConverter.toEntity(
                request, accountId, balanceAfter, Currency.USD, Direction.OUT
        );
        Transaction transaction2 = transactionConverter.toEntity(
                request, accountId, balanceAfter, Currency.USD, Direction.OUT
        );

        assertThat(transaction1.getBusinessId()).isNotNull();
        assertThat(transaction2.getBusinessId()).isNotNull();
        assertThat(transaction1.getBusinessId()).isNotEqualTo(transaction2.getBusinessId());
    }

    @Test
    void toEntityShouldSetCreatedAtAndUpdatedAtToCurrentTime() {
        TransactionRequest request = new TransactionRequest(
                BigDecimal.TEN,
                "EUR",
                "IN",
                "Test"
        );
        LocalDateTime before = LocalDateTime.now();

        Transaction result = transactionConverter.toEntity(
                request, 1L, BigDecimal.TEN, Currency.EUR, Direction.IN
        );

        LocalDateTime after = LocalDateTime.now();

        assertThat(result.getCreatedAt()).isBetween(before, after);
        assertThat(result.getUpdatedAt()).isBetween(before, after);
        assertThat(result.getCreatedAt()).isEqualTo(result.getUpdatedAt());
    }

    @Test
    void toEntityShouldHandleDifferentCurrencies() {
        TransactionRequest eurRequest = new TransactionRequest(
                BigDecimal.TEN, "EUR", "IN", "EUR Transaction"
        );
        TransactionRequest usdRequest = new TransactionRequest(
                BigDecimal.ONE, "USD", "OUT", "USD Transaction"
        );

        Transaction eurTransaction = transactionConverter.toEntity(
                eurRequest, 1L, BigDecimal.TEN, Currency.EUR, Direction.IN
        );
        Transaction usdTransaction = transactionConverter.toEntity(
                usdRequest, 2L, BigDecimal.ONE, Currency.USD, Direction.OUT
        );

        assertThat(eurTransaction.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(usdTransaction.getCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    void toEntityShouldHandleBothDirections() {
        TransactionRequest inRequest = new TransactionRequest(
                BigDecimal.TEN, "EUR", "IN", "IN Transaction"
        );
        TransactionRequest outRequest = new TransactionRequest(
                BigDecimal.ONE, "USD", "OUT", "OUT Transaction"
        );

        Transaction inTransaction = transactionConverter.toEntity(
                inRequest, 1L, BigDecimal.TEN, Currency.EUR, Direction.IN
        );
        Transaction outTransaction = transactionConverter.toEntity(
                outRequest, 2L, BigDecimal.ONE, Currency.USD, Direction.OUT
        );

        assertThat(inTransaction.getDirection()).isEqualTo(Direction.IN);
        assertThat(outTransaction.getDirection()).isEqualTo(Direction.OUT);
    }

    @Test
    void toEntityShouldHandleDifferentAccountIds() {
        TransactionRequest request = new TransactionRequest(
                BigDecimal.TEN, "EUR", "IN", "Test"
        );

        Transaction transaction1 = transactionConverter.toEntity(
                request, 100L, BigDecimal.TEN, Currency.EUR, Direction.IN
        );
        Transaction transaction2 = transactionConverter.toEntity(
                request, 200L, BigDecimal.TEN, Currency.EUR, Direction.IN
        );

        assertThat(transaction1.getAccountId()).isEqualTo(100L);
        assertThat(transaction2.getAccountId()).isEqualTo(200L);
    }

    @Test
    void toEntityShouldHandleLargeAmounts() {
        BigDecimal largeAmount = new BigDecimal("999999999999.99");
        TransactionRequest request = new TransactionRequest(
                largeAmount, "EUR", "IN", "Large amount"
        );

        Transaction result = transactionConverter.toEntity(
                request, 1L, largeAmount, Currency.EUR, Direction.IN
        );

        assertThat(result.getAmount()).isEqualTo(largeAmount);
        assertThat(result.getBalanceAfter()).isEqualTo(largeAmount);
    }

    @Test
    void toEntityShouldHandleZeroBalanceAfter() {
        TransactionRequest request = new TransactionRequest(
                BigDecimal.TEN, "EUR", "OUT", "Zero balance"
        );

        Transaction result = transactionConverter.toEntity(
                request, 1L, BigDecimal.ZERO, Currency.EUR, Direction.OUT
        );

        assertThat(result.getBalanceAfter()).isZero();
    }

    @Test
    void toResponseShouldMapAllFieldsCorrectly() {
        UUID accountBusinessId = UUID.randomUUID();
        UUID transactionBusinessId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .businessId(transactionBusinessId)
                .accountId(1L)
                .amount(BigDecimal.TEN)
                .currency(Currency.EUR)
                .direction(Direction.IN)
                .description("Test transaction")
                .balanceAfter(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionResponse result = transactionConverter.toResponse(transaction, accountBusinessId);

        assertThat(result).isNotNull();
        assertThat(result.accountId()).isEqualTo(accountBusinessId);
        assertThat(result.transactionId()).isEqualTo(transactionBusinessId);
        assertThat(result.amount()).isEqualTo(BigDecimal.TEN);
        assertThat(result.currency()).isEqualTo(Currency.EUR);
        assertThat(result.direction()).isEqualTo(Direction.IN);
        assertThat(result.description()).isEqualTo("Test transaction");
        assertThat(result.balanceAfter()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void toResponseShouldHandleDifferentAccountBusinessIds() {
        UUID accountBusinessId1 = UUID.randomUUID();
        UUID accountBusinessId2 = UUID.randomUUID();
        UUID transactionBusinessId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .businessId(transactionBusinessId)
                .accountId(1L)
                .amount(BigDecimal.ONE)
                .currency(Currency.USD)
                .direction(Direction.OUT)
                .description("Test")
                .balanceAfter(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionResponse response1 = transactionConverter.toResponse(transaction, accountBusinessId1);
        TransactionResponse response2 = transactionConverter.toResponse(transaction, accountBusinessId2);

        assertThat(response1.accountId()).isEqualTo(accountBusinessId1);
        assertThat(response2.accountId()).isEqualTo(accountBusinessId2);
        assertThat(response1.transactionId()).isEqualTo(response2.transactionId());
    }

    @Test
    void toResponseShouldHandleBothDirections() {
        UUID accountBusinessId = UUID.randomUUID();

        Transaction inTransaction = Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(1L)
                .amount(BigDecimal.TEN)
                .currency(Currency.EUR)
                .direction(Direction.IN)
                .description("IN")
                .balanceAfter(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Transaction outTransaction = Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(1L)
                .amount(BigDecimal.TEN)
                .currency(Currency.EUR)
                .direction(Direction.OUT)
                .description("OUT")
                .balanceAfter(BigDecimal.valueOf(90))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TransactionResponse inResponse = transactionConverter.toResponse(inTransaction, accountBusinessId);
        TransactionResponse outResponse = transactionConverter.toResponse(outTransaction, accountBusinessId);

        assertThat(inResponse.direction()).isEqualTo(Direction.IN);
        assertThat(outResponse.direction()).isEqualTo(Direction.OUT);
    }

    @Test
    void toResponsesShouldReturnListOfResponses() {
        UUID accountBusinessId = UUID.randomUUID();
        UUID businessId1 = UUID.randomUUID();
        UUID businessId2 = UUID.randomUUID();

        Transaction transaction1 = Transaction.builder()
                .businessId(businessId1)
                .accountId(1L)
                .amount(BigDecimal.TEN)
                .currency(Currency.EUR)
                .direction(Direction.IN)
                .description("First")
                .balanceAfter(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .businessId(businessId2)
                .accountId(1L)
                .amount(BigDecimal.ONE)
                .currency(Currency.USD)
                .direction(Direction.OUT)
                .description("Second")
                .balanceAfter(BigDecimal.valueOf(90))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Transaction> transactions = List.of(transaction1, transaction2);

        List<TransactionResponse> result = transactionConverter.toResponses(transactions, accountBusinessId);

        assertThat(result)
                .isNotNull()
                .hasSize(2);
        assertThat(result.get(0).transactionId()).isEqualTo(businessId1);
        assertThat(result.get(0).accountId()).isEqualTo(accountBusinessId);
        assertThat(result.get(1).transactionId()).isEqualTo(businessId2);
        assertThat(result.get(1).accountId()).isEqualTo(accountBusinessId);
    }

    @Test
    void toResponsesShouldReturnEmptyListWhenNoTransactions() {
        UUID accountBusinessId = UUID.randomUUID();
        List<Transaction> emptyList = Collections.emptyList();

        List<TransactionResponse> result = transactionConverter.toResponses(emptyList, accountBusinessId);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void toResponsesShouldHandleSingleTransaction() {
        UUID accountBusinessId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .businessId(businessId)
                .accountId(1L)
                .amount(BigDecimal.TEN)
                .currency(Currency.EUR)
                .direction(Direction.IN)
                .description("Single")
                .balanceAfter(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Transaction> transactions = List.of(transaction);

        List<TransactionResponse> result = transactionConverter.toResponses(transactions, accountBusinessId);

        assertThat(result)
                .isNotNull()
                .hasSize(1);
        assertThat(result.getFirst().transactionId()).isEqualTo(businessId);
    }

    @Test
    void toResponsesShouldPreserveOrderOfTransactions() {
        UUID accountBusinessId = UUID.randomUUID();

        Transaction transaction1 = Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(1L)
                .amount(BigDecimal.ONE)
                .currency(Currency.EUR)
                .direction(Direction.IN)
                .description("First")
                .balanceAfter(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(1L)
                .amount(BigDecimal.TEN)
                .currency(Currency.USD)
                .direction(Direction.OUT)
                .description("Second")
                .balanceAfter(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Transaction transaction3 = Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(1L)
                .amount(BigDecimal.ZERO)
                .currency(Currency.EUR)
                .direction(Direction.IN)
                .description("Third")
                .balanceAfter(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Transaction> transactions = List.of(transaction1, transaction2, transaction3);

        List<TransactionResponse> result = transactionConverter.toResponses(transactions, accountBusinessId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).description()).isEqualTo("First");
        assertThat(result.get(1).description()).isEqualTo("Second");
        assertThat(result.get(2).description()).isEqualTo("Third");
    }

    @Test
    void toResponsesShouldUseSameAccountBusinessIdForAllResponses() {
        UUID accountBusinessId = UUID.randomUUID();

        Transaction transaction1 = Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(1L)
                .amount(BigDecimal.ONE)
                .currency(Currency.EUR)
                .direction(Direction.IN)
                .description("Test1")
                .balanceAfter(BigDecimal.ONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(1L)
                .amount(BigDecimal.TEN)
                .currency(Currency.USD)
                .direction(Direction.OUT)
                .description("Test2")
                .balanceAfter(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Transaction> transactions = List.of(transaction1, transaction2);

        List<TransactionResponse> result = transactionConverter.toResponses(transactions, accountBusinessId);

        assertThat(result)
                .hasSize(2)
                .allMatch(response -> response.accountId().equals(accountBusinessId));
    }
}