package com.tuum.corebanking.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.common.dto.PageResponse;
import com.tuum.corebanking.exception.handler.GlobalExceptionHandler;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import com.tuum.corebanking.transaction.dto.response.TransactionResponse;
import com.tuum.corebanking.transaction.model.Direction;
import com.tuum.corebanking.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TransactionController.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void createTransactionSuccessfully() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(150.00), "EUR", "IN", "Salary payment");

        TransactionResponse response = new TransactionResponse(
                accountId,
                transactionId,
                BigDecimal.valueOf(150.00),
                Currency.EUR,
                Direction.IN,
                "Salary payment",
                BigDecimal.valueOf(1150.00)
        );

        when(transactionService.create(eq(accountId), any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts/" + accountId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.direction").value("IN"))
                .andExpect(jsonPath("$.description").value("Salary payment"))
                .andExpect(jsonPath("$.balanceAfter").value(1150.00));

        verify(transactionService).create(eq(accountId), any(TransactionRequest.class));
    }

    @Test
    void createTransactionReturnsBadRequestWhenIdIsInvalidUuid() throws Exception {
        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(150.00), "EUR", "IN", "Salary payment");

        mockMvc.perform(post("/api/accounts/invalid-uuid/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void findByAccountIdSuccessfully() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        TransactionResponse transactionResponse = new TransactionResponse(
                accountId,
                transactionId,
                BigDecimal.valueOf(50.00),
                Currency.EUR,
                Direction.OUT,
                "Coffee",
                BigDecimal.valueOf(1100.00)
        );
        List<TransactionResponse> responseList = List.of(transactionResponse);
        PageResponse<TransactionResponse> pageResponse = new PageResponse<>(responseList, 0, 100, 1);

        when(transactionService.findByAccountId(eq(accountId), eq(0), eq(100))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/accounts/" + accountId + "/transactions")
                        .param("page", "0")
                        .param("size", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.data[0].transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.data[0].amount").value(50.00))
                .andExpect(jsonPath("$.data[0].currency").value("EUR"))
                .andExpect(jsonPath("$.data[0].direction").value("OUT"))
                .andExpect(jsonPath("$.data[0].description").value("Coffee"))
                .andExpect(jsonPath("$.data[0].balanceAfter").value(1100.00))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(transactionService).findByAccountId(eq(accountId), eq(0), eq(100));
    }

    @Test
    void findByAccountIdSuccessfullyWithCustomPagination() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        TransactionResponse transactionResponse = new TransactionResponse(
                accountId,
                transactionId,
                BigDecimal.valueOf(50.00),
                Currency.EUR,
                Direction.OUT,
                "Coffee",
                BigDecimal.valueOf(1100.00)
        );
        List<TransactionResponse> responseList = List.of(transactionResponse);
        PageResponse<TransactionResponse> pageResponse = new PageResponse<>(responseList, 1, 20, 25);

        when(transactionService.findByAccountId(eq(accountId), intThat(i -> i == 1), intThat(i -> i == 20))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/accounts/" + accountId + "/transactions")
                        .param("page", "1")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(transactionService).findByAccountId(eq(accountId), intThat(i -> i == 1), intThat(i -> i == 20));
    }

    @Test
    void findByAccountIdReturnsBadRequestWhenIdIsInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/accounts/invalid-uuid/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }
}