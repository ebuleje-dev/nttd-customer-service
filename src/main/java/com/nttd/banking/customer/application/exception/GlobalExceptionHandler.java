package com.nttd.banking.customer.application.exception;

import com.nttd.banking.customer.model.dto.ErrorResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Global exception handler for the application.
 *
 * @author NTT Data
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles CustomerNotFoundException.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 404 status
   */
  @ExceptionHandler(CustomerNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCustomerNotFound(
      CustomerNotFoundException ex, ServerWebExchange exchange) {
    log.warn("Customer not found: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handles DuplicateCustomerException.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 409 status
   */
  @ExceptionHandler(DuplicateCustomerException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateCustomer(
      DuplicateCustomerException ex, ServerWebExchange exchange) {
    log.warn("Duplicate customer: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handles BusinessValidationException.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 400 status
   */
  @ExceptionHandler(BusinessValidationException.class)
  public ResponseEntity<ErrorResponse> handleBusinessValidation(
      BusinessValidationException ex, ServerWebExchange exchange) {
    log.warn("Business validation failed: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles Bean Validation exceptions.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 400 status
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      WebExchangeBindException ex, ServerWebExchange exchange) {
    log.warn("Validation errors: {}", ex.getMessage());

    List<String> details = new ArrayList<>();
    for (FieldError error : ex.getFieldErrors()) {
      details.add(error.getField() + ": " + error.getDefaultMessage());
    }

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            exchange.getRequest().getPath().value());
    error.setDetails(details);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles IllegalArgumentException.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 400 status
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, ServerWebExchange exchange) {
    log.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles any uncaught exception.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 500 status
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleGenericError(
      RuntimeException ex, ServerWebExchange exchange) {
    log.error("Unexpected error occurred", ex);

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected error occurred. Please contact support.",
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
