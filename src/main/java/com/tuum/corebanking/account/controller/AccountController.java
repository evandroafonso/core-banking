package com.tuum.corebanking.account.controller;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    
    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody @Valid AccountRequest accountRequest) {
        log.info("Received request to create account with customer ID: {}", accountRequest.customerId());
        AccountResponse savedAccount = accountService.create(accountRequest);
        log.info("Account created successfully with business ID: {}", savedAccount.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> findById(@PathVariable UUID accountId) {
        log.info("Received request to find account with ID: {}", accountId);
        AccountResponse accountResponse = accountService.findById(accountId);
        log.debug("Account found: {}", accountId);
        return ResponseEntity.ok(accountResponse);
    }

}