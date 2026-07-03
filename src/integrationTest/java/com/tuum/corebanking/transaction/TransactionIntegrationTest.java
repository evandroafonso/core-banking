package com.tuum.corebanking.transaction;

import com.tuum.corebanking.IntegrationTestBase;
import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.transaction.dto.request.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionIntegrationTest extends IntegrationTestBase {

    private static final String ACCOUNT_CREATED_QUEUE = "account_created_queue";
    private static final String BALANCE_CREATED_QUEUE = "balance_created_queue";
    private static final String BALANCE_UPDATE_QUEUE = "balance_update_queue";
    private static final String TRANSACTION_CREATED_QUEUE = "transaction_created_queue";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        while (rabbitTemplate.receive(ACCOUNT_CREATED_QUEUE, 50) != null) {
        }
        while (rabbitTemplate.receive(BALANCE_CREATED_QUEUE, 50) != null) {
        }
        while (rabbitTemplate.receive(BALANCE_UPDATE_QUEUE, 50) != null) {
        }
        while (rabbitTemplate.receive(TRANSACTION_CREATED_QUEUE, 50) != null) {
        }
    }

    @Test
    void shouldCreateInTransaction_increaseBalance_andPublishEvent() throws Exception {
        UUID accountId = createAccount(List.of("EUR"));
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("150.50"), "EUR", "IN", "Initial Deposit"
        );

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.amount").value(150.50))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.direction").value("IN"))
                .andExpect(jsonPath("$.balanceAfter").value(150.50));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(TRANSACTION_CREATED_QUEUE);
            assertThat(message).isNotNull();

            Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);
            assertThat(payload.get("eventName")).isEqualTo("TransactionCreated");
            assertThat(payload.get("operationType")).isEqualTo("INSERT");
        });
    }

    @Test
    void shouldCreateOutTransaction_decreaseBalance_whenFundsAreSufficient() throws Exception {
        UUID accountId = createAccount(List.of("EUR"));

        TransactionRequest depositRequest = new TransactionRequest(
                new BigDecimal("200.00"), "EUR", "IN", "Salary"
        );
        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isCreated());

        TransactionRequest withdrawRequest = new TransactionRequest(
                new BigDecimal("50.00"), "EUR", "OUT", "Groceries"
        );
        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.direction").value("OUT"))
                .andExpect(jsonPath("$.balanceAfter").value(150.00));
    }

    @Test
    void shouldReturnUnprocessableEntity_whenInsufficientFunds() throws Exception {
        UUID accountId = createAccount(List.of("EUR"));

        TransactionRequest withdrawRequest = new TransactionRequest(
                new BigDecimal("500.00"), "EUR", "OUT", "Buying a TV"
        );

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldReturnBadRequest_whenTransactionAmountIsZeroOrNegative() throws Exception {
        UUID accountId = createAccount(List.of("EUR"));

        TransactionRequest invalidRequest = new TransactionRequest(
                new BigDecimal("-10.00"), "EUR", "IN", "Invalid amount"
        );

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFound_whenAccountDoesNotExist() throws Exception {
        UUID randomAccountId = UUID.randomUUID();
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"), "EUR", "IN", "Deposit"
        );

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", randomAccountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnTransactionsList_whenFindByAccountId() throws Exception {
        UUID accountId = createAccount(List.of("EUR"));

        TransactionRequest deposit1 = new TransactionRequest(new BigDecimal("100"), "EUR", "IN", "T1");
        TransactionRequest deposit2 = new TransactionRequest(new BigDecimal("50"), "EUR", "IN", "T2");

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deposit1)));

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deposit2)));

        mockMvc.perform(get("/api/accounts/{accountId}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].description").value("T2"))
                .andExpect(jsonPath("$.data[1].description").value("T1"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    private UUID createAccount(List<String> currencies) throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest request = new AccountRequest(customerId, "EE", currencies);

        String responseBody = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }


}