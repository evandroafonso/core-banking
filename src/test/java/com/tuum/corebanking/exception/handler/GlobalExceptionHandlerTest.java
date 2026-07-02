package com.tuum.corebanking.exception.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tuum.corebanking.balance.model.Currency;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientFunds(new InsufficientFundsException(BigDecimal.TEN));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody())
                .isNotNull()
                .returns(ErrorCode.INSUFFICIENT_FUNDS.name(), errorResponse -> errorResponse != null ? errorResponse.errorCode() : null);
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
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
    }

    @Test
    void handleHttpMessageNotReadableWithEnumShouldReturnBadRequest() {
        InvalidFormatException cause = mock(InvalidFormatException.class);
        JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);

        when(reference.getDescription()).thenReturn("currency");
        when(cause.getPath()).thenReturn(List.of(reference));
        when(cause.getTargetType()).thenReturn((Class) Currency.class);
        when(cause.getValue()).thenReturn("XYZ");

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Msg", cause, null);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("Invalid value 'XYZ' for field 'currency'");
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
    }

    @Test
    void handleHttpMessageNotReadableWithNumberShouldReturnBadRequest() {
        InvalidFormatException cause = mock(InvalidFormatException.class);
        JsonMappingException.Reference reference = mock(JsonMappingException.Reference.class);

        when(reference.getDescription()).thenReturn("amount");
        when(cause.getPath()).thenReturn(List.of(reference));
        when(cause.getTargetType()).thenReturn((Class) Integer.class);

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Msg", cause, null);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Invalid numeric value for field 'amount'. The value is out of range or improperly formatted.");
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
    }

    @Test
    void handleHttpMessageNotReadableGenericShouldReturnBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Msg", (Throwable) null, null);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Malformed JSON request");
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
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

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid value 'invalid-uuid' for parameter 'accountId'");
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleNoResourceFoundSuccessfully() {
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getMessage()).thenReturn("No static resource api/unknown.");

        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("No static resource api/unknown.");
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.NOT_FOUND.name());
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}