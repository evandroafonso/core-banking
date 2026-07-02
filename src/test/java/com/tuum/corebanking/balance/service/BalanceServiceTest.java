package com.tuum.corebanking.balance.service;

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
        Long accountId = 1L;

        Balance balance1 = new Balance();
        Balance balance2 = new Balance();
        List<Balance> balances = List.of(balance1, balance2);

        BalanceResponse response1 = new BalanceResponse(BigDecimal.ZERO, Currency.EUR);
        BalanceResponse response2 = new BalanceResponse(BigDecimal.ZERO, Currency.USD);
        List<BalanceResponse> expectedResponses = List.of(response1, response2);

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenReturn(expectedResponses);

        List<BalanceResponse> actualResponses = balanceService.create(currencies, accountId);

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
        Long accountId = 1L;
        List<Balance> balances = Collections.emptyList();
        List<BalanceResponse> expectedResponses = Collections.emptyList();

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenReturn(expectedResponses);

        List<BalanceResponse> actualResponses = balanceService.create(currencies, accountId);

        assertNotNull(actualResponses);
        assertTrue(actualResponses.isEmpty());

        verify(balanceConverter).toEntities(currencies, accountId);
        verify(balanceMapper, never()).insert(any(Balance.class));
        verify(balanceConverter).toResponses(balances);
    }

    @Test
    void createBalancesThrowsExceptionWhenConverterFailsToEntities() {
        List<Currency> currencies = List.of(Currency.EUR);
        Long accountId = 1L;

        when(balanceConverter.toEntities(currencies, accountId)).thenThrow(new RuntimeException("Conversion error"));

        assertThrows(RuntimeException.class, () -> balanceService.create(currencies, accountId));

        verify(balanceConverter).toEntities(currencies, accountId);
        verifyNoInteractions(balanceMapper);
    }

    @Test
    void createBalancesThrowsExceptionWhenMapperInsertFails() {
        List<Currency> currencies = List.of(Currency.EUR);
        Long accountId = 1L;

        Balance balance = new Balance();
        List<Balance> balances = List.of(balance);

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        doThrow(new RuntimeException("Database error")).when(balanceMapper).insert(balance);

        assertThrows(RuntimeException.class, () -> balanceService.create(currencies, accountId));

        verify(balanceConverter).toEntities(currencies, accountId);
        verify(balanceMapper).insert(balance);
        verify(balanceConverter, never()).toResponses(any());
    }

    @Test
    void createBalancesThrowsExceptionWhenConverterFailsToResponses() {
        List<Currency> currencies = List.of(Currency.EUR);
        Long accountId = 1L;

        Balance balance = new Balance();
        List<Balance> balances = List.of(balance);

        when(balanceConverter.toEntities(currencies, accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenThrow(new RuntimeException("Response mapping error"));

        assertThrows(RuntimeException.class, () -> balanceService.create(currencies, accountId));

        verify(balanceConverter).toEntities(currencies, accountId);
        verify(balanceMapper).insert(balance);
        verify(balanceConverter).toResponses(balances);
    }

    @Test
    void findByAccountIdSuccessfully() {
        Long accountId = 1L;

        Balance balance1 = new Balance();
        Balance balance2 = new Balance();
        List<Balance> balances = List.of(balance1, balance2);

        BalanceResponse response1 = new BalanceResponse(BigDecimal.ZERO, Currency.EUR);
        BalanceResponse response2 = new BalanceResponse(BigDecimal.ZERO, Currency.USD);
        List<BalanceResponse> expectedResponses = List.of(response1, response2);

        when(balanceMapper.findByAccountId(accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenReturn(expectedResponses);

        List<BalanceResponse> actualResponses = balanceService.findByAccountId(accountId);

        assertNotNull(actualResponses);
        assertEquals(expectedResponses.size(), actualResponses.size());
        assertEquals(expectedResponses, actualResponses);

        verify(balanceMapper).findByAccountId(accountId);
        verify(balanceConverter).toResponses(balances);
    }

    @Test
    void findByAccountIdReturnsEmptyListWhenNoBalancesFound() {
        Long accountId = 1L;
        List<Balance> balances = Collections.emptyList();
        List<BalanceResponse> expectedResponses = Collections.emptyList();

        when(balanceMapper.findByAccountId(accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenReturn(expectedResponses);

        List<BalanceResponse> actualResponses = balanceService.findByAccountId(accountId);

        assertNotNull(actualResponses);
        assertTrue(actualResponses.isEmpty());

        verify(balanceMapper).findByAccountId(accountId);
        verify(balanceConverter).toResponses(balances);
    }

    @Test
    void findByAccountIdThrowsExceptionWhenMapperFails() {
        Long accountId = 1L;

        when(balanceMapper.findByAccountId(accountId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> balanceService.findByAccountId(accountId));

        verify(balanceMapper).findByAccountId(accountId);
        verifyNoInteractions(balanceConverter);
    }

    @Test
    void findByAccountIdThrowsExceptionWhenConverterFails() {
        Long accountId = 1L;
        Balance balance = new Balance();
        List<Balance> balances = List.of(balance);

        when(balanceMapper.findByAccountId(accountId)).thenReturn(balances);
        when(balanceConverter.toResponses(balances)).thenThrow(new RuntimeException("Mapping error"));

        assertThrows(RuntimeException.class, () -> balanceService.findByAccountId(accountId));

        verify(balanceMapper).findByAccountId(accountId);
        verify(balanceConverter).toResponses(balances);
    }
}