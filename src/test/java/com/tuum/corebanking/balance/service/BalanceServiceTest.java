package com.tuum.corebanking.balance.service;

import com.tuum.corebanking.balance.converter.BalanceConverter;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.event.BalanceCreatedEvent;
import com.tuum.corebanking.balance.event.BalanceUpdateEvent;
import com.tuum.corebanking.balance.mapper.BalanceMapper;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.exception.ResourceNotFoundException;
import com.tuum.corebanking.messaging.event.OperationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

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

        verify(applicationEventPublisher, times(2)).publishEvent(any(BalanceCreatedEvent.class));
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

    @Test
    void findBalanceWithLockShouldReturnBalanceWhenExists() {
        Long accountId = 1L;
        UUID accountBusinessId = UUID.randomUUID();
        Currency currency = Currency.EUR;
        Balance expectedBalance = Balance.builder()
                .id(10L)
                .accountId(accountId)
                .currency(currency)
                .availableAmount(BigDecimal.TEN)
                .build();

        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, currency))
                .thenReturn(Optional.of(expectedBalance));

        Balance actualBalance = balanceService.findBalanceWithLock(accountId, accountBusinessId, currency);

        assertNotNull(actualBalance);
        assertEquals(expectedBalance, actualBalance);

        verify(balanceMapper).findByAccountIdAndCurrencyForUpdate(accountId, currency);
    }

    @Test
    void findBalanceWithLockShouldThrowAccountNotFoundExceptionWhenBalanceDoesNotExist() {
        Long accountId = 1L;
        UUID accountBusinessId = UUID.randomUUID();
        Currency currency = Currency.EUR;

        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, currency))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> balanceService.findBalanceWithLock(accountId, accountBusinessId, currency)
        );

        assertTrue(exception.getMessage().contains("No balance found for account"));
        assertTrue(exception.getMessage().contains(accountBusinessId.toString()));
        assertTrue(exception.getMessage().contains(currency.name()));

        verify(balanceMapper).findByAccountIdAndCurrencyForUpdate(accountId, currency);
    }

    @Test
    void findBalanceWithLockShouldThrowExceptionWhenMapperFails() {
        Long accountId = 1L;
        UUID accountBusinessId = UUID.randomUUID();
        Currency currency = Currency.EUR;

        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, currency))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> balanceService.findBalanceWithLock(accountId, accountBusinessId, currency));

        verify(balanceMapper).findByAccountIdAndCurrencyForUpdate(accountId, currency);
    }

    @Test
    void findBalanceWithLockShouldWorkWithDifferentCurrencies() {
        Long accountId = 1L;
        UUID accountBusinessId = UUID.randomUUID();

        Balance eurBalance = Balance.builder()
                .id(10L)
                .accountId(accountId)
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.TEN)
                .build();

        Balance usdBalance = Balance.builder()
                .id(11L)
                .accountId(accountId)
                .currency(Currency.USD)
                .availableAmount(BigDecimal.ONE)
                .build();

        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, Currency.EUR))
                .thenReturn(Optional.of(eurBalance));
        when(balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, Currency.USD))
                .thenReturn(Optional.of(usdBalance));

        Balance actualEurBalance = balanceService.findBalanceWithLock(accountId, accountBusinessId, Currency.EUR);
        Balance actualUsdBalance = balanceService.findBalanceWithLock(accountId, accountBusinessId, Currency.USD);

        assertEquals(eurBalance, actualEurBalance);
        assertEquals(usdBalance, actualUsdBalance);

        verify(balanceMapper).findByAccountIdAndCurrencyForUpdate(accountId, Currency.EUR);
        verify(balanceMapper).findByAccountIdAndCurrencyForUpdate(accountId, Currency.USD);
    }

    @Test
    void updateShouldSucceedWhenRowsAffected() {
        Balance balance = Balance.builder()
                .id(10L)
                .businessId(UUID.randomUUID())
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.TEN)
                .build();
        BigDecimal balanceAfter = BigDecimal.valueOf(20);

        when(balanceMapper.updateAvailableAmount(balance.getId(), balanceAfter)).thenReturn(1);

        balanceService.update(balance, balanceAfter);

        verify(balanceMapper).updateAvailableAmount(balance.getId(), balanceAfter);

        ArgumentCaptor<BalanceUpdateEvent> eventCaptor = ArgumentCaptor.forClass(BalanceUpdateEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        BalanceUpdateEvent capturedEvent = eventCaptor.getValue();
        assertEquals("BalanceUpdated", capturedEvent.eventName());
        assertEquals(OperationType.UPDATE, capturedEvent.operationType());
        assertEquals(balanceAfter, capturedEvent.payload().balance());
        assertEquals(Currency.EUR, capturedEvent.payload().currency());
        assertNotNull(capturedEvent.occurredAt());
    }

    @Test
    void updateShouldThrowIllegalStateExceptionWhenNoRowsAffected() {
        Balance balance = Balance.builder()
                .id(10L)
                .businessId(UUID.randomUUID())
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.TEN)
                .build();
        BigDecimal balanceAfter = BigDecimal.valueOf(20);

        when(balanceMapper.updateAvailableAmount(balance.getId(), balanceAfter)).thenReturn(0);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> balanceService.update(balance, balanceAfter)
        );

        assertTrue(exception.getMessage().contains("Failed to update balance, no rows affected for id"));
        assertTrue(exception.getMessage().contains(balance.getBusinessId().toString()));

        verify(balanceMapper).updateAvailableAmount(balance.getId(), balanceAfter);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void updateShouldThrowExceptionWhenMapperFails() {
        Balance balance = Balance.builder()
                .id(10L)
                .businessId(UUID.randomUUID())
                .currency(Currency.USD)
                .availableAmount(BigDecimal.TEN)
                .build();
        BigDecimal balanceAfter = BigDecimal.valueOf(20);

        when(balanceMapper.updateAvailableAmount(balance.getId(), balanceAfter))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> balanceService.update(balance, balanceAfter));

        verify(balanceMapper).updateAvailableAmount(balance.getId(), balanceAfter);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void updateShouldPublishEventWithCorrectBalanceAfterAndCurrency() {
        Balance balance = Balance.builder()
                .id(10L)
                .businessId(UUID.randomUUID())
                .currency(Currency.USD)
                .availableAmount(BigDecimal.valueOf(50))
                .build();
        BigDecimal balanceAfter = BigDecimal.valueOf(30);

        when(balanceMapper.updateAvailableAmount(balance.getId(), balanceAfter)).thenReturn(1);

        balanceService.update(balance, balanceAfter);

        ArgumentCaptor<BalanceUpdateEvent> eventCaptor = ArgumentCaptor.forClass(BalanceUpdateEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        BalanceUpdateEvent capturedEvent = eventCaptor.getValue();
        assertEquals(BigDecimal.valueOf(30), capturedEvent.payload().balance());
        assertEquals(Currency.USD, capturedEvent.payload().currency());
    }

    @Test
    void updateShouldHandleZeroBalanceAfter() {
        Balance balance = Balance.builder()
                .id(10L)
                .businessId(UUID.randomUUID())
                .currency(Currency.EUR)
                .availableAmount(BigDecimal.TEN)
                .build();
        BigDecimal balanceAfter = BigDecimal.ZERO;

        when(balanceMapper.updateAvailableAmount(balance.getId(), balanceAfter)).thenReturn(1);

        balanceService.update(balance, balanceAfter);

        ArgumentCaptor<BalanceUpdateEvent> eventCaptor = ArgumentCaptor.forClass(BalanceUpdateEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        BalanceUpdateEvent capturedEvent = eventCaptor.getValue();
        assertEquals(BigDecimal.ZERO, capturedEvent.payload().balance());
        assertEquals(Currency.EUR, capturedEvent.payload().currency());
    }
}