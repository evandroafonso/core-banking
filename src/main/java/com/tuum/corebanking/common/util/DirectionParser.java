package com.tuum.corebanking.common.util;

import com.tuum.corebanking.exception.InvalidTransactionDirectionException;
import com.tuum.corebanking.transaction.model.Direction;

public final class DirectionParser {

    public static Direction parse(String raw) {
        return EnumParser.parse(
                raw,
                Direction.class,
                value -> new InvalidTransactionDirectionException(
                        "Invalid direction: '%s'. Accepted values: %s"
                                .formatted(value, EnumParser.acceptedValues(Direction.class))
                )
        );
    }
}