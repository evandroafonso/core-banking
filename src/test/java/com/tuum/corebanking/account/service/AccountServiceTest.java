package com.tuum.corebanking.account.service;

import com.tuum.corebanking.account.converter.AccountConverter;
import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.account.dto.response.AccountResponse;
import com.tuum.corebanking.account.event.AccountCreatedEvent;
import com.tuum.corebanking.account.mapper.AccountMapper;
import com.tuum.corebanking.account.model.Account;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.balance.service.BalanceService;
import com.tuum.corebanking.exception.InvalidCurrencyException;
import com.tuum.corebanking.exception.ResourceNotFoundException;
import com.tuum.corebanking.messaging.event.OperationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountConverter accountConverter;

    @Mock
    private BalanceService balanceService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AccountService accountService;

    @Captor
    private ArgumentCaptor<AccountCreatedEvent> eventCaptor;

    @Test
    void createWithValidRequestShouldCreateAccountAndPublishEvent() {
        AccountRequest request = mock(AccountRequest.class);
        when(request.currencies()).thenReturn(List.of("EUR"));

        Account account = new Account();
        account.setId(100L);

        AccountResponse expectedResponse = mock(AccountResponse.class);
        List<BalanceResponse> balances = List.of(mock(BalanceResponse.class));

        when(accountConverter.toEntity(request)).thenReturn(account);
        when(balanceService.create(List.of(Currency.EUR), 100L)).thenReturn(balances);
        when(accountConverter.toResponse(account, balances)).thenReturn(expectedResponse);

        AccountResponse actualResponse = accountService.create(request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(account.getBusinessId()).isNotNull();
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isNotNull();

        verify(accountMapper).insert(account);
        verify(balanceService).create(List.of(Currency.EUR), 100L);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        AccountCreatedEvent publishedEvent = eventCaptor.getValue();

        assertThat(publishedEvent.eventName()).isEqualTo("AccountCreated");
        assertThat(publishedEvent.operationType()).isEqualTo(OperationType.INSERT);
        assertThat(publishedEvent.payload()).isEqualTo(expectedResponse);
    }

    @Test
    void createWithDuplicateAndMessyCurrenciesShouldFormatAndDeduplicate() {
        AccountRequest request = mock(AccountRequest.class);
        when(request.currencies()).thenReturn(List.of(" eur ", "EUR", "uSd", " USD "));

        Account account = new Account();
        account.setId(200L);

        when(accountConverter.toEntity(request)).thenReturn(account);
        when(balanceService.create(any(), eq(200L))).thenReturn(List.of());

        accountService.create(request);

        verify(balanceService).create(List.of(Currency.EUR, Currency.EUR, Currency.USD, Currency.USD), 200L);
    }

    @Test
    void createWithInvalidCurrencyShouldThrowInvalidCurrencyException() {
        AccountRequest request = mock(AccountRequest.class);
        String invalidCurrencyCode = "XYZ";
        when(request.currencies()).thenReturn(List.of("EUR", invalidCurrencyCode));

        String expectedAcceptedValues = Arrays.toString(Currency.values());

        assertThatThrownBy(() -> accountService.create(request))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessage("Invalid currency: 'XYZ'. Accepted values: " + expectedAcceptedValues);

        verify(accountConverter, never()).toEntity(any());
        verify(accountMapper, never()).insert(any());
        verify(balanceService, never()).create(any(), any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void findByIdSuccessfully() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Long internalId = 1L;

        Account account = new Account();
        account.setId(internalId);

        List<BalanceResponse> balancesResponse = List.of(new BalanceResponse(BigDecimal.ZERO, Currency.EUR));
        AccountResponse expectedResponse = new AccountResponse(accountId, customerId, balancesResponse);

        when(accountMapper.findByBusinessId(accountId)).thenReturn(Optional.of(account));
        when(balanceService.findByAccountId(internalId)).thenReturn(balancesResponse);
        when(accountConverter.toResponse(account, balancesResponse)).thenReturn(expectedResponse);

        AccountResponse actualResponse = accountService.findById(accountId);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(accountMapper).findByBusinessId(accountId);
        verify(balanceService).findByAccountId(internalId);
        verify(accountConverter).toResponse(account, balancesResponse);
    }

    @Test
    void findByIdThrowsAccountNotFoundExceptionWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();

        when(accountMapper.findByBusinessId(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.findById(accountId));

        verify(accountMapper).findByBusinessId(accountId);
        verifyNoInteractions(balanceService);
        verifyNoInteractions(accountConverter);
    }

    @Test
    void findByIdThrowsExceptionWhenBalanceServiceFails() {
        UUID accountId = UUID.randomUUID();
        Long internalId = 1L;
        Account account = new Account();
        account.setId(internalId);

        when(accountMapper.findByBusinessId(accountId)).thenReturn(Optional.of(account));
        when(balanceService.findByAccountId(internalId)).thenThrow(new RuntimeException("Service error"));

        assertThrows(RuntimeException.class, () -> accountService.findById(accountId));

        verify(accountMapper).findByBusinessId(accountId);
        verify(balanceService).findByAccountId(internalId);
        verifyNoInteractions(accountConverter);
    }

    @Test
    void findByIdThrowsExceptionWhenConverterFails() {
        UUID accountId = UUID.randomUUID();
        Long internalId = 1L;
        Account account = new Account();
        account.setId(internalId);
        List<BalanceResponse> balancesResponse = Collections.emptyList();

        when(accountMapper.findByBusinessId(accountId)).thenReturn(Optional.of(account));
        when(balanceService.findByAccountId(internalId)).thenReturn(balancesResponse);
        when(accountConverter.toResponse(account, balancesResponse)).thenThrow(new RuntimeException("Converter error"));

        assertThrows(RuntimeException.class, () -> accountService.findById(accountId));

        verify(accountMapper).findByBusinessId(accountId);
        verify(balanceService).findByAccountId(internalId);
        verify(accountConverter).toResponse(account, balancesResponse);
    }

    @Test
    void findAccountIdByBusinessIdShouldReturnAccountIdWhenAccountExists() {
        UUID accountBusinessId = UUID.randomUUID();
        Long expectedAccountId = 1L;

        when(accountMapper.findAccountIdByBusinessId(accountBusinessId))
                .thenReturn(Optional.of(expectedAccountId));

        Long actualAccountId = accountService.findAccountIdByBusinessId(accountBusinessId);

        assertThat(actualAccountId)
                .isNotNull()
                .isEqualTo(expectedAccountId);

        verify(accountMapper).findAccountIdByBusinessId(accountBusinessId);
    }

    @Test
    void findAccountIdByBusinessIdShouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        UUID accountBusinessId = UUID.randomUUID();

        when(accountMapper.findAccountIdByBusinessId(accountBusinessId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findAccountIdByBusinessId(accountBusinessId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found with id")
                .hasMessageContaining(accountBusinessId.toString());

        verify(accountMapper).findAccountIdByBusinessId(accountBusinessId);
    }

    @Test
    void findAccountIdByBusinessIdShouldHandleMultipleDifferentIds() {
        UUID accountBusinessId1 = UUID.randomUUID();
        UUID accountBusinessId2 = UUID.randomUUID();
        Long expectedAccountId1 = 1L;
        Long expectedAccountId2 = 2L;

        when(accountMapper.findAccountIdByBusinessId(accountBusinessId1))
                .thenReturn(Optional.of(expectedAccountId1));
        when(accountMapper.findAccountIdByBusinessId(accountBusinessId2))
                .thenReturn(Optional.of(expectedAccountId2));

        Long actualAccountId1 = accountService.findAccountIdByBusinessId(accountBusinessId1);
        Long actualAccountId2 = accountService.findAccountIdByBusinessId(accountBusinessId2);

        assertThat(actualAccountId1).isEqualTo(expectedAccountId1);
        assertThat(actualAccountId2).isEqualTo(expectedAccountId2);

        verify(accountMapper).findAccountIdByBusinessId(accountBusinessId1);
        verify(accountMapper).findAccountIdByBusinessId(accountBusinessId2);
    }

    @Test
    void findAccountIdByBusinessIdShouldThrowExceptionWithCorrectMessageFormat() {
        UUID accountBusinessId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String expectedMessage = "Account not found with id: 123e4567-e89b-12d3-a456-426614174000";

        when(accountMapper.findAccountIdByBusinessId(accountBusinessId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findAccountIdByBusinessId(accountBusinessId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);

        verify(accountMapper).findAccountIdByBusinessId(accountBusinessId);
    }
}