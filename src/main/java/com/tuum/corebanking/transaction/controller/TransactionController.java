package com.tuum.corebanking.transaction.controller;

import com.tuum.corebanking.common.dto.PageResponse;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @PathVariable UUID accountId,
            @RequestBody @Valid TransactionRequest request) {
        log.info("Received request to create transaction for account: {}, amount: {}, currency: {}, direction: {}",
                accountId, request.amount(), request.currency(), request.direction());
        TransactionResponse response = transactionService.create(accountId, request);
        log.info("Transaction created successfully with ID: {} for account: {}", response.transactionId(), accountId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> findByAccountId(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        log.info("Received request to find transactions for account: {}, page: {}, size: {}", accountId, page, size);
        PageResponse<TransactionResponse> response = transactionService.findByAccountId(accountId, page, size);
        log.info("Returning {} transactions for account: {} (page: {}, total: {})",
                response.data().size(), accountId, page, response.totalElements());
        return ResponseEntity.ok(response);
    }
}