package com.tuum.corebanking.transaction.controller;

import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @PathVariable UUID accountId,
            @RequestBody @Valid TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(accountId, request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findByAccountId(
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(transactionService.findByAccountId(accountId));
    }
}