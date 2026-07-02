package com.tuum.corebanking.transaction.converter;

import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.model.Direction;
import com.tuum.corebanking.transaction.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionConverter {

    public Transaction toEntity(TransactionRequest request, Long accountId, BigDecimal balanceAfter,
                                Currency currency, Direction direction) {
        return Transaction.builder()
                .businessId(UUID.randomUUID())
                .accountId(accountId)
                .amount(request.amount())
                .currency(currency)
                .direction(direction)
                .description(request.description())
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction, UUID accountBusinessId) {
        return new TransactionResponse(
                accountBusinessId,
                transaction.getBusinessId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDirection(),
                transaction.getDescription(),
                transaction.getBalanceAfter()
        );
    }

    public List<TransactionResponse> toResponses(List<Transaction> transactions, UUID accountBusinessId) {
        return transactions.stream()
                .map(transaction -> toResponse(transaction, accountBusinessId))
                .toList();
    }
}