package com.tuum.corebanking.common.util;

import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.exception.InvalidCurrencyException;

import java.util.List;

public final class CurrencyParser {

    public static Currency parse(String raw) {
        return EnumParser.parse(
                raw,
                Currency.class,
                value -> new InvalidCurrencyException(
                        "Invalid currency: '%s'. Accepted values: %s"
                                .formatted(value, EnumParser.acceptedValues(Currency.class))
                )
        );
    }

    public static List<Currency> parseList(List<String> rawValues) {
        return EnumParser.parseList(
                rawValues,
                Currency.class,
                value -> new InvalidCurrencyException(
                        "Invalid currency: '%s'. Accepted values: %s"
                                .formatted(value, EnumParser.acceptedValues(Currency.class))
                )
        );
    }
}