package com.tuum.corebanking.common.util;

import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyParserTest {

    // Testes para o método parse(String raw)

    private static Stream<Arguments> provideValidCurrencyStrings() {
        return Stream.of(
                Arguments.of("EUR", Currency.EUR),
                Arguments.of("USD", Currency.USD),
                Arguments.of("GBP", Currency.GBP),
                Arguments.of("eur", Currency.EUR),
                Arguments.of("usd", Currency.USD),
                Arguments.of("gbp", Currency.GBP),
                Arguments.of("Eur", Currency.EUR),
                Arguments.of("Usd", Currency.USD),
                Arguments.of("eUr", Currency.EUR)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidCurrencyStrings")
    void parseShouldReturnCurrencyWhenValidString(String input, Currency expectedCurrency) {
        Currency result = CurrencyParser.parse(input);

        assertThat(result).isEqualTo(expectedCurrency);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BRL", "JPY", "ABC", "XYZ", "123", "eurr", "usdd", ""})
    void parseShouldThrowInvalidCurrencyExceptionWhenInvalidString(String invalidCurrency) {
        assertThatThrownBy(() -> CurrencyParser.parse(invalidCurrency))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Invalid currency")
                .hasMessageContaining(invalidCurrency)
                .hasMessageContaining("Accepted values");
    }

    @Test
    void parseShouldContainAllAcceptedValuesInErrorMessage() {
        String invalidCurrency = "BRL";

        assertThatThrownBy(() -> CurrencyParser.parse(invalidCurrency))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Accepted values")
                .satisfies(exception -> {
                    String message = exception.getMessage();
                    for (Currency currency : Currency.values()) {
                        assertThat(message).contains(currency.name());
                    }
                });
    }

    // Testes para o método parseList(List<String> rawValues)

    @Test
    void parseListShouldReturnCurrenciesWhenAllValid() {
        List<String> input = List.of("EUR", "USD", "GBP");
        List<Currency> expected = List.of(Currency.EUR, Currency.USD, Currency.GBP);

        List<Currency> result = CurrencyParser.parseList(input);

        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .isEqualTo(expected);
    }

    @Test
    void parseListShouldReturnEmptyListWhenInputIsEmpty() {
        List<String> input = Collections.emptyList();

        List<Currency> result = CurrencyParser.parseList(input);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void parseListShouldHandleSingleElement() {
        List<String> input = List.of("EUR");
        List<Currency> expected = List.of(Currency.EUR);

        List<Currency> result = CurrencyParser.parseList(input);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .isEqualTo(expected);
    }

    @Test
    void parseListShouldThrowInvalidCurrencyExceptionWhenAnyElementIsInvalid() {
        List<String> input = List.of("EUR", "BRL", "USD");

        assertThatThrownBy(() -> CurrencyParser.parseList(input))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Invalid currency")
                .hasMessageContaining("BRL")
                .hasMessageContaining("Accepted values");
    }

    @Test
    void parseListShouldThrowInvalidCurrencyExceptionWhenFirstElementIsInvalid() {
        List<String> input = List.of("INVALID", "EUR", "USD");

        assertThatThrownBy(() -> CurrencyParser.parseList(input))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("INVALID");
    }

    @Test
    void parseListShouldThrowInvalidCurrencyExceptionWhenLastElementIsInvalid() {
        List<String> input = List.of("EUR", "USD", "WRONG");

        assertThatThrownBy(() -> CurrencyParser.parseList(input))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("WRONG");
    }

    @Test
    void parseListShouldHandleDuplicatedValidCurrencies() {
        List<String> input = List.of("EUR", "EUR", "USD");
        List<Currency> expected = List.of(Currency.EUR, Currency.EUR, Currency.USD);

        List<Currency> result = CurrencyParser.parseList(input);

        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .isEqualTo(expected);
    }

    @Test
    void parseListShouldHandleAllCurrenciesOfEnum() {
        List<String> input = Arrays.stream(Currency.values())
                .map(Enum::name)
                .toList();
        List<Currency> expected = List.of(Currency.values());

        List<Currency> result = CurrencyParser.parseList(input);

        assertThat(result)
                .isNotNull()
                .hasSize(Currency.values().length)
                .isEqualTo(expected);
    }

    @Test
    void parseListShouldWorkWithCaseInsensitiveValues() {
        List<String> input = List.of("eur", "USD", "gbp");
        List<Currency> expected = List.of(Currency.EUR, Currency.USD, Currency.GBP);

        List<Currency> result = CurrencyParser.parseList(input);

        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .isEqualTo(expected);
    }

    @Test
    void parseListShouldThrowExceptionContainingAcceptedValues() {
        List<String> input = List.of("EUR", "INVALID_CURRENCY", "USD");

        assertThatThrownBy(() -> CurrencyParser.parseList(input))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("Accepted values")
                .satisfies(exception -> {
                    String message = exception.getMessage();
                    for (Currency currency : Currency.values()) {
                        assertThat(message).contains(currency.name());
                    }
                });
    }

    @Test
    void parseListShouldThrowExceptionOnFirstInvalidElement() {
        List<String> input = List.of("FIRST_INVALID", "SECOND_INVALID");

        assertThatThrownBy(() -> CurrencyParser.parseList(input))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining("FIRST_INVALID")
                .satisfies(exception -> {
                    String message = exception.getMessage();
                    assertThat(message).doesNotContain("SECOND_INVALID");
                });
    }
}