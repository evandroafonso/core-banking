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
import com.tuum.corebanking.messaging.event.OperationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(balanceService.create(List.of(Currency.EUR), request, 100L)).thenReturn(balances);
        when(accountConverter.toResponse(account, balances)).thenReturn(expectedResponse);

        AccountResponse actualResponse = accountService.create(request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(account.getBusinessId()).isNotNull();
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isNotNull();

        verify(accountMapper).insert(account);
        verify(balanceService).create(List.of(Currency.EUR), request, 100L);

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
        when(balanceService.create(any(), eq(request), eq(200L))).thenReturn(List.of());

        accountService.create(request);

        verify(balanceService).create(List.of(Currency.EUR, Currency.USD), request, 200L);
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
        verify(balanceService, never()).create(any(), any(), any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}