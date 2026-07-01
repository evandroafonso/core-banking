package com.tuum.corebanking.balance.service;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.balance.converter.BalanceConverter;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.mapper.BalanceMapper;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private BalanceMapper balanceMapper;

    @Mock
    private BalanceConverter balanceConverter;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    void createBalancesSuccessfully() {
        List<Currency> currencies = List.of(Currency.EUR, Currency.USD);
        AccountRequest accountRequest = new AccountRequest(UUID.randomUUID(), "EE", List.of("EUR", "USD"));
        Long accountId = 1L;

        Balance balance1 = new Balance();
        Balance balance2 = new Balance();
        List<Balance> balances = List.of(balance1, balance2);

        BalanceResponse response1 = new BalanceResponse(BigDecimal.ZERO, Currency.EUR);
        BalanceResponse response2 = new BalanceResponse(BigDecimal.ZERO, Currency.USD);
        List<BalanceResponse> expectedResponses = List.of(response1, response2);

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenReturn(expectedResponses);

        List<BalanceResponse> actualResponses = balanceService.create(currencies, accountRequest, accountId);

        assertNotNull(actualResponses);
        assertEquals(expectedResponses.size(), actualResponses.size());
        assertEquals(expectedResponses, actualResponses);

        verify(balanceConverter).toEntities(currencies, accountId);
        verify(balanceMapper).insert(balance1);
        verify(balanceMapper).insert(balance2);
        verify(balanceConverter).toResponses(balances);
    }

    @Test
    void createBalancesWithEmptyCurrenciesList() {
        List<Currency> currencies = Collections.emptyList();
        AccountRequest accountRequest = new AccountRequest(UUID.randomUUID(), "EE", Collections.emptyList());
        Long accountId = 1L;
        List<Balance> balances = Collections.emptyList();
        List<BalanceResponse> expectedResponses = Collections.emptyList();

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenReturn(expectedResponses);

        List<BalanceResponse> actualResponses = balanceService.create(currencies, accountRequest, accountId);

        assertNotNull(actualResponses);
        assertTrue(actualResponses.isEmpty());

        verify(balanceConverter).toEntities(currencies, accountId);
        verify(balanceMapper, never()).insert(any(Balance.class));
        verify(balanceConverter).toResponses(balances);
    }

    @Test
    void createBalancesThrowsExceptionWhenConverterFailsToEntities() {
        List<Currency> currencies = List.of(Currency.EUR);
        AccountRequest accountRequest = new AccountRequest(UUID.randomUUID(), "EE", List.of("EUR"));
        Long accountId = 1L;

        when(balanceConverter.toEntities(currencies, accountId)).thenThrow(new RuntimeException("Conversion error"));

        assertThrows(RuntimeException.class, () -> balanceService.create(currencies, accountRequest, accountId));

        verify(balanceConverter).toEntities(currencies, accountId);
        verifyNoInteractions(balanceMapper);
    }

    @Test
    void createBalancesThrowsExceptionWhenMapperInsertFails() {
        List<Currency> currencies = List.of(Currency.EUR);
        AccountRequest accountRequest = new AccountRequest(UUID.randomUUID(), "EE", List.of("EUR"));
        Long accountId = 1L;

        Balance balance = new Balance();
        List<Balance> balances = List.of(balance);

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        doThrow(new RuntimeException("Database error")).when(balanceMapper).insert(balance);

        assertThrows(RuntimeException.class, () -> balanceService.create(currencies, accountRequest, accountId));

        verify(balanceConverter).toEntities(currencies, accountId);
        verify(balanceMapper).insert(balance);
        verify(balanceConverter, never()).toResponses(any());
    }

    @Test
    void createBalancesThrowsExceptionWhenConverterFailsToResponses() {
        List<Currency> currencies = List.of(Currency.EUR);
        AccountRequest accountRequest = new AccountRequest(UUID.randomUUID(), "EE", List.of("EUR"));
        Long accountId = 1L;

        Balance balance = new Balance();
        List<Balance> balances = List.of(balance);

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenThrow(new RuntimeException("Response mapping error"));

        assertThrows(RuntimeException.class, () -> balanceService.create(currencies, accountRequest, accountId));

        verify(balanceConverter).toEntities(currencies, accountId);
        verify(balanceMapper).insert(balance);
        verify(balanceConverter).toResponses(balances);
    }
}