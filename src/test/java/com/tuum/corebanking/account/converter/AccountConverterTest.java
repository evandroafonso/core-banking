package com.tuum.corebanking.account.converter;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.model.Account;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.model.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountConverterTest {

    @InjectMocks
    private AccountConverter accountConverter;

    @Test
    void convertToEntitySuccessfully() {
        UUID customerId = UUID.randomUUID();
        AccountRequest request = new AccountRequest(customerId, "EE", List.of("EUR"));

        Account account = accountConverter.toEntity(request);

        assertNotNull(account);
        assertEquals(customerId, account.getCustomerId());
        assertEquals("EE", account.getCountry());
    }

    @Test
    void convertToResponseSuccessfully() {
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder()
                .businessId(accountId)
                .customerId(customerId)
                .country("EE")
                .build();

        List<BalanceResponse> balancesResponse = List.of(
                new BalanceResponse(BigDecimal.ZERO, Currency.EUR)
        );

        AccountResponse response = accountConverter.toResponse(account, balancesResponse);

        assertNotNull(response);
        assertEquals(accountId, response.id());
        assertEquals(customerId, response.customerId());
        assertEquals(balancesResponse, response.balances());
    }

    @Test
    void convertToEntityThrowsExceptionWhenRequestIsNull() {
        assertThrows(NullPointerException.class, () -> accountConverter.toEntity(null));
    }

    @Test
    void convertToResponseThrowsExceptionWhenAccountIsNull() {
        List<BalanceResponse> balancesResponse = Collections.emptyList();
        assertThrows(NullPointerException.class, () -> accountConverter.toResponse(null, balancesResponse));
    }
}