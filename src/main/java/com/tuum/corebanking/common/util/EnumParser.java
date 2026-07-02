package com.tuum.corebanking.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class EnumParser {
    
    public static <T extends Enum<T>> T parse(
            String raw,
            Class<T> enumClass,
            Function<String, RuntimeException> exceptionSupplier) {
        try {
            return Enum.valueOf(enumClass, raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw exceptionSupplier.apply(raw);
        }
    }

    public static <T extends Enum<T>> List<T> parseList(
            List<String> rawValues,
            Class<T> enumClass,
            Function<String, RuntimeException> exceptionSupplier) {
        return rawValues.stream()
                .map(raw -> parse(raw, enumClass, exceptionSupplier))
                .toList();
    }

    public static <T extends Enum<T>> String acceptedValues(Class<T> enumClass) {
        return Arrays.toString(enumClass.getEnumConstants());
    }
}
