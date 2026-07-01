package com.tuum.corebanking.balance.converter;

import com.tuum.corebanking.account.dto.request.AccountRequest;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BalanceConverterTest {

    private final BalanceConverter balanceConverter = new BalanceConverter();

    @Test
    void toEntitiesShouldMapCurrenciesToBalances() {
        List<Currency> currencies = List.of(Currency.EUR, Currency.USD);
        AccountRequest request = mock(AccountRequest.class);
        Long accountId = 1L;

        List<Balance> result = balanceConverter.toEntities(currencies, accountId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Balance::getCurrency).containsExactlyInAnyOrder(Currency.EUR, Currency.USD);
        assertThat(result).allSatisfy(balance -> {
            assertThat(balance.getAccountId()).isEqualTo(accountId);
            assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(balance.getBusinessId()).isNotNull();
            assertThat(balance.getCreatedAt()).isNotNull();
            assertThat(balance.getUpdatedAt()).isNotNull();
        });
    }

    @Test
    void toResponsesShouldMapBalancesToResponses() {
        Balance b1 = Balance.builder()
                .currency(Currency.EUR)
                .balance(new BigDecimal("100.00"))
                .build();
        Balance b2 = Balance.builder()
                .currency(Currency.USD)
                .balance(new BigDecimal("50.00"))
                .build();
        List<Balance> balances = List.of(b1, b2);

        List<BalanceResponse> responses = balanceConverter.toResponses(balances);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(BalanceResponse::currency)
                .containsExactlyInAnyOrder(Currency.EUR, Currency.USD);
    }
}