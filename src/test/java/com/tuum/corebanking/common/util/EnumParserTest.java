package com.tuum.corebanking.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumParserTest {

    private static Stream<Arguments> provideValidParseScenarios() {
        return Stream.of(
                Arguments.of(TestEnum.class, "VALUE1", TestEnum.VALUE1),
                Arguments.of(TestEnum.class, "VALUE2", TestEnum.VALUE2),
                Arguments.of(TestEnum.class, "VALUE3", TestEnum.VALUE3),
                Arguments.of(Color.class, "RED", Color.RED),
                Arguments.of(Color.class, "GREEN", Color.GREEN),
                Arguments.of(Color.class, "BLUE", Color.BLUE),
                Arguments.of(Size.class, "SMALL", Size.SMALL),
                Arguments.of(Size.class, "MEDIUM", Size.MEDIUM),
                Arguments.of(Size.class, "LARGE", Size.LARGE)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidParseScenarios")
    void parseShouldReturnEnumWhenValidString(Class<? extends Enum<?>> enumClass,
                                              String input,
                                              Enum<?> expected) {
        @SuppressWarnings("unchecked")
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid value: " + value);

        Enum<?> result = EnumParser.parse(input, (Class) enumClass, exceptionSupplier);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void parseShouldHandleUpperCaseInput() {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        TestEnum result = EnumParser.parse("VALUE1", TestEnum.class, exceptionSupplier);

        assertThat(result).isEqualTo(TestEnum.VALUE1);
    }

    // Testes para o método parse()

    @Test
    void parseShouldHandleLowerCaseInput() {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        TestEnum result = EnumParser.parse("value1", TestEnum.class, exceptionSupplier);

        assertThat(result).isEqualTo(TestEnum.VALUE1);
    }

    @Test
    void parseShouldHandleMixedCaseInput() {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        TestEnum result = EnumParser.parse("Value1", TestEnum.class, exceptionSupplier);

        assertThat(result).isEqualTo(TestEnum.VALUE1);
    }

    @Test
    void parseShouldTrimWhitespaceFromInput() {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        TestEnum result1 = EnumParser.parse("  VALUE1  ", TestEnum.class, exceptionSupplier);
        TestEnum result2 = EnumParser.parse("\tVALUE2\t", TestEnum.class, exceptionSupplier);
        TestEnum result3 = EnumParser.parse("\nVALUE3\n", TestEnum.class, exceptionSupplier);

        assertThat(result1).isEqualTo(TestEnum.VALUE1);
        assertThat(result2).isEqualTo(TestEnum.VALUE2);
        assertThat(result3).isEqualTo(TestEnum.VALUE3);
    }

    @Test
    void parseShouldThrowExceptionWhenInvalidValue() {
        String invalidValue = "INVALID";
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Custom error for: " + value);

        assertThatThrownBy(() -> EnumParser.parse(invalidValue, TestEnum.class, exceptionSupplier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Custom error for: " + invalidValue);
    }

    @Test
    void parseShouldUseExceptionSupplierWithOriginalRawValue() {
        String rawValue = "  invalid  ";
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Error with value: '" + value + "'");

        assertThatThrownBy(() -> EnumParser.parse(rawValue, TestEnum.class, exceptionSupplier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(rawValue);
    }

    @Test
    void parseShouldThrowCustomRuntimeException() {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new UnsupportedOperationException("Not supported: " + value);

        assertThatThrownBy(() -> EnumParser.parse("UNKNOWN", TestEnum.class, exceptionSupplier))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Not supported: UNKNOWN");
    }

    @Test
    void parseListShouldReturnEnumsWhenAllValid() {
        List<String> input = List.of("VALUE1", "VALUE2", "VALUE3");
        List<TestEnum> expected = List.of(TestEnum.VALUE1, TestEnum.VALUE2, TestEnum.VALUE3);
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(input, TestEnum.class, exceptionSupplier);

        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .isEqualTo(expected);
    }

    @Test
    void parseListShouldReturnEmptyListWhenInputIsEmpty() {
        List<String> input = Collections.emptyList();
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(input, TestEnum.class, exceptionSupplier);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void parseListShouldHandleSingleElement() {
        List<String> input = List.of("VALUE1");
        List<TestEnum> expected = List.of(TestEnum.VALUE1);
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(input, TestEnum.class, exceptionSupplier);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .isEqualTo(expected);
    }

    // Testes para o método parseList()

    @Test
    void parseListShouldThrowExceptionWhenAnyElementIsInvalid() {
        List<String> input = List.of("VALUE1", "INVALID", "VALUE3");
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        assertThatThrownBy(() -> EnumParser.parseList(input, TestEnum.class, exceptionSupplier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid: INVALID");
    }

    @Test
    void parseListShouldStopOnFirstInvalidElement() {
        List<String> input = List.of("FIRST_INVALID", "SECOND_INVALID");
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Error: " + value);

        assertThatThrownBy(() -> EnumParser.parseList(input, TestEnum.class, exceptionSupplier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Error: FIRST_INVALID");
    }

    @Test
    void parseListShouldHandleCaseInsensitiveValues() {
        List<String> input = List.of("value1", "VALUE2", "Value3");
        List<TestEnum> expected = List.of(TestEnum.VALUE1, TestEnum.VALUE2, TestEnum.VALUE3);
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(input, TestEnum.class, exceptionSupplier);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void parseListShouldHandleWhitespaceInElements() {
        List<String> input = List.of("  VALUE1  ", " VALUE2", "VALUE3 ");
        List<TestEnum> expected = List.of(TestEnum.VALUE1, TestEnum.VALUE2, TestEnum.VALUE3);
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(input, TestEnum.class, exceptionSupplier);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void parseListShouldThrowNullPointerExceptionWhenListIsNull() {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        assertThatThrownBy(() -> EnumParser.parseList(null, TestEnum.class, exceptionSupplier))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void parseListShouldWorkWithDifferentEnumTypes() {
        List<String> colorInput = List.of("RED", "GREEN", "BLUE");
        List<Color> expectedColors = List.of(Color.RED, Color.GREEN, Color.BLUE);
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<Color> result = EnumParser.parseList(colorInput, Color.class, exceptionSupplier);

        assertThat(result).isEqualTo(expectedColors);
    }

    @Test
    void parseListShouldHandleDuplicates() {
        List<String> input = List.of("VALUE1", "VALUE1", "VALUE2");
        List<TestEnum> expected = List.of(TestEnum.VALUE1, TestEnum.VALUE1, TestEnum.VALUE2);
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(input, TestEnum.class, exceptionSupplier);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void parseListShouldHandleAllEnumValues() {
        List<String> input = Arrays.stream(TestEnum.values())
                .map(Enum::name)
                .toList();
        List<TestEnum> expected = List.of(TestEnum.values());
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(input, TestEnum.class, exceptionSupplier);

        assertThat(result)
                .hasSize(TestEnum.values().length)
                .isEqualTo(expected);
    }

    @Test
    void acceptedValuesShouldReturnAllEnumConstants() {
        String result = EnumParser.acceptedValues(TestEnum.class);

        assertThat(result)
                .isNotNull()
                .contains("VALUE1")
                .contains("VALUE2")
                .contains("VALUE3");
    }

    @Test
    void acceptedValuesShouldBeInBracketsFormat() {
        String result = EnumParser.acceptedValues(Color.class);

        assertThat(result)
                .startsWith("[")
                .endsWith("]");
    }

    @Test
    void acceptedValuesShouldContainAllColors() {
        String result = EnumParser.acceptedValues(Color.class);

        for (Color color : Color.values()) {
            assertThat(result).contains(color.name());
        }
    }

    // Testes para o método acceptedValues()

    @Test
    void acceptedValuesShouldContainAllSizes() {
        String result = EnumParser.acceptedValues(Size.class);

        for (Size size : Size.values()) {
            assertThat(result).contains(size.name());
        }
    }

    @Test
    void acceptedValuesShouldBeCommaSeparated() {
        String result = EnumParser.acceptedValues(TestEnum.class);

        assertThat(result).contains(", ");
    }

    @Test
    void acceptedValuesShouldMatchArraysToString() {
        String expected = Arrays.toString(TestEnum.values());
        String result = EnumParser.acceptedValues(TestEnum.class);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void parseAndAcceptedValuesShouldBeConsistent() {
        String acceptedValues = EnumParser.acceptedValues(TestEnum.class);

        // Verifica que cada valor aceito pode ser parseado
        for (TestEnum value : TestEnum.values()) {
            Function<String, RuntimeException> exceptionSupplier =
                    val -> new IllegalArgumentException("Invalid: " + val);

            TestEnum parsed = EnumParser.parse(value.name(), TestEnum.class, exceptionSupplier);
            assertThat(parsed).isEqualTo(value);

            // Verifica que o valor está na lista de aceitos
            assertThat(acceptedValues).contains(value.name());
        }
    }

    @Test
    void parseListShouldBeComposableWithAcceptedValues() {
        List<String> allValues = Arrays.stream(TestEnum.values())
                .map(Enum::name)
                .toList();
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        List<TestEnum> result = EnumParser.parseList(allValues, TestEnum.class, exceptionSupplier);

        assertThat(result)
                .hasSize(TestEnum.values().length)
                .containsExactly(TestEnum.values());
    }

    @Test
    void parseWithCustomExceptionShouldPreserveStackTrace() {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new RuntimeException("Custom error for: " + value);

        assertThatThrownBy(() -> EnumParser.parse("UNKNOWN", TestEnum.class, exceptionSupplier))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Custom error for: UNKNOWN")
                .hasStackTraceContaining("EnumParser.parse");
    }

    // Testes de integração entre os métodos

    @Test
    void parseShouldBeThreadSafe() throws InterruptedException {
        Function<String, RuntimeException> exceptionSupplier =
                value -> new IllegalArgumentException("Invalid: " + value);

        Runnable task = () -> {
            TestEnum result = EnumParser.parse("VALUE1", TestEnum.class, exceptionSupplier);
            assertThat(result).isEqualTo(TestEnum.VALUE1);
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);
        Thread thread3 = new Thread(task);

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();
    }

    // Enum de exemplo para testes
    private enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    private enum Color {
        RED, GREEN, BLUE
    }

    private enum Size {
        SMALL, MEDIUM, LARGE
    }
}