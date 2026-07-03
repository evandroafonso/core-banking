package com.tuum.corebanking.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tuum.corebanking.exception.*;
import com.tuum.corebanking.exception.model.ErrorCode;
import com.tuum.corebanking.exception.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse buildError(String message, ErrorCode errorCode, HttpStatus status) {
        return new ErrorResponse(
                message,
                errorCode.name(),
                status.value(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return ResponseEntity.unprocessableContent()
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.INSUFFICIENT_FUNDS,
                        HttpStatus.UNPROCESSABLE_CONTENT
                ));
    }

    @ExceptionHandler(InvalidCurrencyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCurrency(InvalidCurrencyException ex) {
        log.warn("Currency validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.INVALID_CURRENCY,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(InvalidTransactionDirectionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionDirection(InvalidTransactionDirectionException ex) {
        log.warn("Transaction direction validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.INVALID_TRANSACTION_DIRECTION,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(InvalidTransactionAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionAmount(InvalidTransactionAmountException ex) {
        log.warn("Transaction amount validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(buildError(
                        ex.getMessage(),
                        ErrorCode.INVALID_TRANSACTION_AMOUNT,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", message);
        return ResponseEntity.badRequest()
                .body(buildError(
                        message,
                        ErrorCode.VALIDATION_ERROR,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Malformed JSON request";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().isEmpty()
                    ? "field"
                    : ife.getPath().getLast().getDescription();

            if (ife.getTargetType().isEnum()) {
                Object[] validValues = ife.getTargetType().getEnumConstants();
                message = String.format(
                        "Invalid value '%s' for field '%s'. Accepted values: %s",
                        ife.getValue(),
                        fieldName,
                        Arrays.toString(validValues)
                );
            } else if (Number.class.isAssignableFrom(ife.getTargetType())
                    || ife.getTargetType().isPrimitive()) {
                message = String.format(
                        "Invalid numeric value for field '%s'. The value is out of range or improperly formatted.",
                        fieldName
                );
            } else {
                message = String.format("Invalid value for field '%s'.", fieldName);
            }
        }

        log.warn("Malformed request: {}", message);
        return ResponseEntity.badRequest()
                .body(buildError(
                        message,
                        ErrorCode.VALIDATION_ERROR,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        log.warn("Type mismatch error: {}", message);
        return ResponseEntity.badRequest()
                .body(buildError(
                        message,
                        ErrorCode.VALIDATION_ERROR,
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(buildError(
                        "Unexpected error",
                        ErrorCode.INTERNAL_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

}