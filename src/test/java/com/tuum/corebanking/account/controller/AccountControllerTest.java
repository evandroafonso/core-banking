package com.tuum.corebanking.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.service.AccountService;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.exception.ResourceNotFoundException;
import com.tuum.corebanking.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AccountController.class, GlobalExceptionHandler.class})
class AccountControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void createAccountSuccessfully() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest accountRequest = new AccountRequest(customerId, "EE", List.of("EUR"));

        List<BalanceResponse> balances = List.of(new BalanceResponse(BigDecimal.ZERO, Currency.EUR));
        AccountResponse accountResponse = new AccountResponse(UUID.randomUUID(), customerId, balances);

        when(accountService.create(any(AccountRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.balances[0].currency").value("EUR"))
                .andExpect(jsonPath("$.balances[0].balance").value(0));

        verify(accountService).create(accountRequest);
    }

    @Test
    void createAccountReturnsBadRequestWhenCustomerIdIsNull() throws Exception {
        AccountRequest invalidRequest = new AccountRequest(null, "EE", List.of("EUR"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("customerId: Customer ID is required"));

        verify(accountService, never()).create(any());
    }

    @Test
    void createAccountReturnsBadRequestWhenCountryIsInvalid() throws Exception {
        AccountRequest invalidRequest = new AccountRequest(UUID.randomUUID(), "INVALID_COUNTRY", List.of("EUR"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("country: Country must be uppercase 2-letter ISO code (e.g. EE, US, GB)"));
        verify(accountService, never()).create(any());
    }

    @Test
    void createAccountReturnsBadRequestWhenCurrenciesIsEmpty() throws Exception {
        AccountRequest invalidRequest = new AccountRequest(UUID.randomUUID(), "EE", Collections.emptyList());

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("currencies: At least one currency is required"));

        verify(accountService, never()).create(any());
    }


    @Test
    void findByIdSuccessfully() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        List<BalanceResponse> balances = List.of(new BalanceResponse(BigDecimal.ZERO, Currency.EUR));
        AccountResponse accountResponse = new AccountResponse(accountId, customerId, balances);

        when(accountService.findById(accountId)).thenReturn(accountResponse);

        mockMvc.perform(get("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.balances[0].currency").value("EUR"))
                .andExpect(jsonPath("$.balances[0].balance").value(0));

        verify(accountService).findById(accountId);
    }

    @Test
    void findByIdReturnsNotFoundWhenAccountDoesNotExist() throws Exception {
        UUID accountId = UUID.randomUUID();

        when(accountService.findById(accountId)).thenThrow(new ResourceNotFoundException("Account not found with id: " + accountId));

        mockMvc.perform(get("/api/accounts/" + accountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService).findById(accountId);
    }

    @Test
    void findByIdReturnsBadRequestWhenIdIsInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/accounts/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

}