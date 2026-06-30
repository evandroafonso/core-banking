package com.tuum.corebanking.account.controller;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest accountRequest) {
        AccountResponse savedAccount = accountService.create(accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

}
