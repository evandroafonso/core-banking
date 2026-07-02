package com.tuum.corebanking.exception.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tuum.corebanking.exception.*;
import com.tuum.corebanking.exception.model.ErrorCode;
import com.tuum.corebanking.exception.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAccountNotFoundShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleAccountNotFound(new AccountNotFoundException("Msg"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.NOT_FOUND.name());
    }

    @Test
    void handleInsufficientFundsShouldReturnUnprocessable() {
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientFunds(new InsufficientFundsException("Msg"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.INSUFFICIENT_FUNDS.name());
    }

    @Test
    void handleInvalidCurrencyShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCurrency(new InvalidCurrencyException("Msg"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.INVALID_CURRENCY.name());
    }

    @Test
    void handleInvalidTransactionDirectionShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidTransactionDirection(new InvalidTransactionDirectionException("Msg"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.INVALID_TRANSACTION_DIRECTION.name());
    }

    @Test
    void handleInvalidTransactionAmountShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidTransactionAmount(new InvalidTransactionAmountException("Msg"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.INVALID_TRANSACTION_AMOUNT.name());
    }

    @Test
    void handleValidationShouldReturnBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "field", "error")));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("field: error");
    }

    @Test
    void handleHttpMessageNotReadableWithEnumShouldReturnBadRequest() {
        InvalidFormatException cause = mock(InvalidFormatException.class);
        when(cause.getTargetType()).thenReturn((Class) Enum.class);
        when(cause.getPath()).thenReturn(List.of(new JsonMappingException.Reference(null, "currency")));

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Msg", cause, null);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleGenericExceptionShouldReturnInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("Error"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.name());
    }

    @Test
    void handleMethodArgumentTypeMismatchSuccessfully() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getValue()).thenReturn("invalid-uuid");
        when(exception.getName()).thenReturn("accountId");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatch(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid value 'invalid-uuid' for parameter 'accountId'", response.getBody().message());
        assertEquals("VALIDATION_ERROR", response.getBody().errorCode());
        assertEquals(400, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleNoResourceFoundSuccessfully() {
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getMessage()).thenReturn("No static resource api/unknown.");

        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No static resource api/unknown.", response.getBody().message());
        assertEquals("NOT_FOUND", response.getBody().errorCode());
        assertEquals(404, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }
}