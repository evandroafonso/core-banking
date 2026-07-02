package com.tuum.corebanking.common.util;

import com.tuum.corebanking.exception.InvalidTransactionDirectionException;
import com.tuum.corebanking.transaction.model.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DirectionParserTest {

    private static Stream<Arguments> provideValidDirectionStrings() {
        return Stream.of(
                Arguments.of("IN", Direction.IN),
                Arguments.of("OUT", Direction.OUT),
                Arguments.of("in", Direction.IN),
                Arguments.of("out", Direction.OUT),
                Arguments.of("In", Direction.IN),
                Arguments.of("Out", Direction.OUT),
                Arguments.of("iN", Direction.IN),
                Arguments.of("oUT", Direction.OUT)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidDirectionStrings")
    void parseShouldReturnDirectionWhenValidString(String input, Direction expectedDirection) {
        Direction result = DirectionParser.parse(input);

        assertThat(result).isEqualTo(expectedDirection);
    }

    @Test
    void parseShouldReturnINWhenValidUpperCase() {
        Direction result = DirectionParser.parse("IN");

        assertThat(result).isEqualTo(Direction.IN);
    }

    @Test
    void parseShouldReturnOUTWhenValidUpperCase() {
        Direction result = DirectionParser.parse("OUT");

        assertThat(result).isEqualTo(Direction.OUT);
    }

    @Test
    void parseShouldReturnINWhenValidLowerCase() {
        Direction result = DirectionParser.parse("in");

        assertThat(result).isEqualTo(Direction.IN);
    }

    @Test
    void parseShouldReturnOUTWhenValidLowerCase() {
        Direction result = DirectionParser.parse("out");

        assertThat(result).isEqualTo(Direction.OUT);
    }

    @Test
    void parseShouldReturnINWhenValidMixedCase() {
        Direction result = DirectionParser.parse("In");

        assertThat(result).isEqualTo(Direction.IN);
    }

    @Test
    void parseShouldReturnOUTWhenValidMixedCase() {
        Direction result = DirectionParser.parse("Out");

        assertThat(result).isEqualTo(Direction.OUT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "INPUT", "OUTPUT", "INOUT", "OUTIN", "ABC", "XYZ", "123", "INN", "OUTT"})
    void parseShouldThrowInvalidTransactionDirectionExceptionWhenInvalidString(String invalidDirection) {
        assertThatThrownBy(() -> DirectionParser.parse(invalidDirection))
                .isInstanceOf(InvalidTransactionDirectionException.class)
                .hasMessageContaining("Invalid direction")
                .hasMessageContaining(invalidDirection)
                .hasMessageContaining("Accepted values");
    }

    @Test
    void parseShouldThrowInvalidTransactionDirectionExceptionWhenWhitespaceString() {
        assertThatThrownBy(() -> DirectionParser.parse("   "))
                .isInstanceOf(InvalidTransactionDirectionException.class)
                .hasMessageContaining("Invalid direction");
    }

    @Test
    void parseShouldContainAllAcceptedValuesInErrorMessage() {
        String invalidDirection = "INVALID_DIRECTION";

        assertThatThrownBy(() -> DirectionParser.parse(invalidDirection))
                .isInstanceOf(InvalidTransactionDirectionException.class)
                .hasMessageContaining("Accepted values")
                .satisfies(exception -> {
                    String message = exception.getMessage();
                    for (Direction direction : Direction.values()) {
                        assertThat(message).contains(direction.name());
                    }
                });
    }

    @Test
    void parseShouldIncludeOriginalValueInErrorMessage() {
        String invalidDirection = "UNKNOWN";

        assertThatThrownBy(() -> DirectionParser.parse(invalidDirection))
                .isInstanceOf(InvalidTransactionDirectionException.class)
                .hasMessageContaining("'%s'".formatted(invalidDirection));
    }

    @Test
    void parseShouldHaveFormattedErrorMessage() {
        String invalidDirection = "WRONG";

        assertThatThrownBy(() -> DirectionParser.parse(invalidDirection))
                .isInstanceOf(InvalidTransactionDirectionException.class)
                .hasMessageStartingWith("Invalid direction")
                .hasMessageContaining("Accepted values")
                .hasMessageContaining("IN")
                .hasMessageContaining("OUT");
    }
    
    @Test
    void parseShouldBeCaseInsensitiveForIN() {
        String[] validInputs = {"IN", "In", "iN", "in"};

        for (String input : validInputs) {
            Direction result = DirectionParser.parse(input);
            assertThat(result)
                    .as("Input '%s' should return Direction.IN", input)
                    .isEqualTo(Direction.IN);
        }
    }

    @Test
    void parseShouldBeCaseInsensitiveForOUT() {
        String[] validInputs = {"OUT", "Out", "oUT", "ouT", "out"};

        for (String input : validInputs) {
            Direction result = DirectionParser.parse(input);
            assertThat(result)
                    .as("Input '%s' should return Direction.OUT", input)
                    .isEqualTo(Direction.OUT);
        }
    }

    @Test
    void parseShouldOnlyAcceptInAndOutCombinations() {
        String[] invalidInputs = {"INN", "OUTT", "INOUT", "OUTIN", "IN OUT", "IN-OUT"};

        for (String input : invalidInputs) {
            assertThatThrownBy(() -> DirectionParser.parse(input))
                    .as("Input '%s' should throw InvalidTransactionDirectionException", input)
                    .isInstanceOf(InvalidTransactionDirectionException.class);
        }
    }

    @Test
    void parseShouldHandleOnlyTwoPossibleValues() {
        Direction[] directions = Direction.values();
        assertThat(directions).hasSize(2);
        assertThat(directions).contains(Direction.IN, Direction.OUT);
    }

    @Test
    void parseShouldNotBeNullForValidInputs() {
        assertThat(DirectionParser.parse("IN")).isNotNull();
        assertThat(DirectionParser.parse("OUT")).isNotNull();
        assertThat(DirectionParser.parse("in")).isNotNull();
        assertThat(DirectionParser.parse("out")).isNotNull();
    }

    @Test
    void parseShouldReturnSameResultForSameInput() {
        String input = "IN";

        Direction result1 = DirectionParser.parse(input);
        Direction result2 = DirectionParser.parse(input);

        assertThat(result1).isSameAs(result2);
    }

    @Test
    void parseShouldDifferentiateBetweenInAndOut() {
        Direction inResult = DirectionParser.parse("IN");
        Direction outResult = DirectionParser.parse("OUT");

        assertThat(inResult).isNotEqualTo(outResult);
        assertThat(inResult).isEqualTo(Direction.IN);
        assertThat(outResult).isEqualTo(Direction.OUT);
    }
}