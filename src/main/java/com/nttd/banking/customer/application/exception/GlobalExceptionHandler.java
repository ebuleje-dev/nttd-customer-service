package com.nttd.banking.customer.application.exception;

import com.nttd.banking.customer.model.dto.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
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
   * Handles CustomerHasActiveProductsException.
   * Thrown when attempting to delete a customer with active products.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 409 status
   */
  @ExceptionHandler(CustomerHasActiveProductsException.class)
  public ResponseEntity<ErrorResponse> handleCustomerHasActiveProducts(
      CustomerHasActiveProductsException ex, ServerWebExchange exchange) {
    log.warn("Cannot delete customer with active products: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Customer Has Active Products",
            ex.getMessage(),
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handles ProfileUpdateNotAllowedException.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 400 status
   */
  @ExceptionHandler(ProfileUpdateNotAllowedException.class)
  public ResponseEntity<ErrorResponse> handleProfileUpdateNotAllowed(
      ProfileUpdateNotAllowedException ex, ServerWebExchange exchange) {
    log.warn("Profile update not allowed: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Profile Update Not Allowed",
            ex.getMessage(),
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
   * Handles TimeoutException from async operations.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 504 status
   */
  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity<ErrorResponse> handleTimeout(
      TimeoutException ex, ServerWebExchange exchange) {
    log.error("Operation timed out: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.GATEWAY_TIMEOUT.value(),
            HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase(),
            "The operation timed out. Please try again later.",
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
  }

  /**
   * Handles CallNotPermittedException from Circuit Breaker.
   * This exception is thrown when the circuit breaker is in OPEN state.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 503 status
   */
  @ExceptionHandler(CallNotPermittedException.class)
  public ResponseEntity<ErrorResponse> handleCircuitBreakerOpen(
      CallNotPermittedException ex, ServerWebExchange exchange) {
    log.warn("Circuit breaker is OPEN: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Service Temporarily Unavailable",
            "The service is temporarily unavailable due to high error rate. "
                + "Please try again later.",
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
  }

  /**
   * Handles RequestNotPermitted from Rate Limiter.
   * This exception is thrown when the rate limit is exceeded.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 429 status
   */
  @ExceptionHandler(RequestNotPermitted.class)
  public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
      RequestNotPermitted ex, ServerWebExchange exchange) {
    log.warn("Rate limit exceeded: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Too Many Requests",
            "Rate limit exceeded. Please wait before making more requests.",
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
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
   * Handles ResponseStatusException from WebFlux.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with the status from the exception
   */
  @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      org.springframework.web.server.ResponseStatusException ex, ServerWebExchange exchange) {
    log.warn("ResponseStatusException: {} {} - {} [{}]",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getPath().value(),
        ex.getStatusCode(),
        ex.getReason());

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            ex.getStatusCode().value(),
            ex.getStatusCode().toString(),
            ex.getReason() != null ? ex.getReason() : "Request processing error",
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(ex.getStatusCode()).body(error);
  }

  /**
   * Handles any uncaught RuntimeException.
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 500 status
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleGenericError(
      RuntimeException ex, ServerWebExchange exchange) {
    log.error("Unexpected RuntimeException: {} {} - {} [{}]",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getPath().value(),
        ex.getMessage(),
        ex.getClass().getSimpleName(),
        ex);

    ErrorResponse error =
        new ErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected error occurred. Please contact support.",
            exchange.getRequest().getPath().value());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  /**
   * Handles any uncaught Exception (catch-all).
   *
   * @param ex the exception
   * @param exchange request information
   * @return ResponseEntity with 500 status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(
      Exception ex, ServerWebExchange exchange) {
    log.error("Catch-all exception handler: {} {} - {} [{}]",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getPath().value(),
        ex.getMessage(),
        ex.getClass().getSimpleName(),
        ex);

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
