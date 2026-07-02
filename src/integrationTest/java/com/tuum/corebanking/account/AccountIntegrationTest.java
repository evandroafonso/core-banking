package com.tuum.corebanking.account;

import com.tuum.corebanking.IntegrationTestBase;
import tools.jackson.databind.ObjectMapper;
import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.mapper.AccountMapper;
import com.tuum.corebanking.account.model.Account;
import com.tuum.corebanking.account.service.AccountService;
import com.tuum.corebanking.exception.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountIntegrationTest extends IntegrationTestBase {

    private static final String ACCOUNT_CREATED_QUEUE = "account_created_queue";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        while (rabbitTemplate.receive(ACCOUNT_CREATED_QUEUE, 50) != null) {
        }
    }

    @Test
    void shouldCreateAccount_persistData_andPublishEvent() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest request = new AccountRequest(customerId, "EE", List.of("EUR", "USD"));

        String responseBody = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.balances", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        UUID accountId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        Account persisted = accountMapper.findByBusinessId(accountId).orElseThrow();
        assertThat(persisted.getCountry()).isEqualTo("EE");
        assertThat(persisted.getCustomerId()).isEqualTo(customerId);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(ACCOUNT_CREATED_QUEUE);
            assertThat(message).isNotNull();

            Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);
            assertThat(payload.get("eventName")).isEqualTo("AccountCreated");
            assertThat(payload.get("operationType")).isEqualTo("INSERT");
        });
    }

    @Test
    void shouldReturnAccount_whenFindById() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest request = new AccountRequest(customerId, "PT", List.of("EUR"));

        String responseBody = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID accountId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.balances[0].currency").value("EUR"));
    }

    @Test
    void shouldReturn400_whenInvalidCountry() throws Exception {
        AccountRequest invalidRequest = new AccountRequest(UUID.randomUUID(), "estonia", List.of("EUR"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenCurrenciesEmpty() throws Exception {
        AccountRequest invalidRequest = new AccountRequest(UUID.randomUUID(), "EE", List.of());

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowAccountNotFoundException_whenBusinessIdDoesNotExist() {
        UUID randomId = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.findAccountIdByBusinessId(randomId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found with id: %s".formatted(randomId));
    }

    @Test
    void shouldReturn404_whenAccountNotFound() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("Account not found")));
    }

    @Test
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn400ForInvalidUUIDFormat() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid value 'abc'")));
    }

    @Test
    void shouldReturn400ForInvalidCurrency() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest accountRequest = new AccountRequest(customerId, "EE", List.of("EUR"));

        String responseBody = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID accountId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        String transactionJson = """
                {
                    "amount": 100.00,
                    "currency": "INVALID",
                    "direction": "IN",
                    "description": "Test transaction"
                }
                """;

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURRENCY"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid currency")))
                .andExpect(jsonPath("$.message").value(containsString("INVALID")));
    }

    @Test
    void shouldReturn400ForInvalidTransactionDirection() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest accountRequest = new AccountRequest(customerId, "EE", List.of("EUR"));

        String responseBody = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID accountId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        String transactionJson = """
                {
                    "amount": 100.00,
                    "currency": "EUR",
                    "direction": "INVALID",
                    "description": "Test transaction"
                }
                """;

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TRANSACTION_DIRECTION"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid direction")))
                .andExpect(jsonPath("$.message").value(containsString("INVALID")));
    }

    @Test
    void shouldReturn400ForInvalidCurrencyInAccountCreation() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest invalidRequest = new AccountRequest(customerId, "EE", List.of("INVALID"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CURRENCY"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid currency")))
                .andExpect(jsonPath("$.message").value(containsString("INVALID")));
    }

    @Test
    void shouldReturn400ForTransactionWithNonExistentCurrencyBalance() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountRequest accountRequest = new AccountRequest(customerId, "EE", List.of("EUR"));

        String responseBody = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID accountId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        String transactionJson = """
                {
                    "amount": 100.00,
                    "currency": "USD",
                    "direction": "IN",
                    "description": "Test transaction"
                }
                """;

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("No balance found")))
                .andExpect(jsonPath("$.message").value(containsString("USD")));
    }

    @Test
    void shouldReturn404ForNonExistentEndpoint() throws Exception {
        mockMvc.perform(get("/api/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

}   