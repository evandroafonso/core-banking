package com.tuum.corebanking.account.controller;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    
    @PostMapping
    public ResponseEntity<AccountResponse> create(@RequestBody @Valid AccountRequest accountRequest) {
        AccountResponse savedAccount = accountService.create(accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> findById(@PathVariable UUID accountId) {
        AccountResponse accountResponse = accountService.findById(accountId);
        return ResponseEntity.ok(accountResponse);
    }

}