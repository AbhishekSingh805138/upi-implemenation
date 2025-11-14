package com.upi.utility.exception;

import com.upi.utility.dto.UtilityErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for utility service
 * Handles all exceptions and returns appropriate error responses with proper HTTP status codes
 */
@RestControllerAdvice
@Slf4j
public class UtilityExceptionHandler {

    /**
     * Handle InsufficientBalanceException
     * Returns HTTP 400 with balance error message
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<UtilityErrorResponse> handleInsufficientBalanceException(
            InsufficientBalanceException ex, WebRequest request) {
        log.error("Insufficient balance error: {}", ex.getMessage());
        
        UtilityErrorResponse errorResponse = UtilityErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Insufficient Balance")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle ProviderUnavailableException
     * Returns HTTP 503 with retry message
     */
    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<UtilityErrorResponse> handleProviderUnavailableException(
            ProviderUnavailableException ex, WebRequest request) {
        log.error("Provider unavailable error: {}", ex.getMessage());
        
        UtilityErrorResponse errorResponse = UtilityErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Service Unavailable")
                .message(ex.getMessage() + ". Please try again later.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handle PaymentProcessingException
     * Returns HTTP 500 with failure details
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<UtilityErrorResponse> handlePaymentProcessingException(
            PaymentProcessingException ex, WebRequest request) {
        log.error("Payment processing error: {}", ex.getMessage());
        
        UtilityErrorResponse errorResponse = UtilityErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Payment Processing Failed")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle InvalidAccountIdentifierException
     * Returns HTTP 400 with validation error
     */
    @ExceptionHandler(InvalidAccountIdentifierException.class)
    public ResponseEntity<UtilityErrorResponse> handleInvalidAccountIdentifierException(
            InvalidAccountIdentifierException ex, WebRequest request) {
        log.error("Invalid account identifier error: {}", ex.getMessage());
        
        UtilityErrorResponse errorResponse = UtilityErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Account Identifier")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle DuplicateTransactionException
     * Returns HTTP 409 with duplicate error
     */
    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<UtilityErrorResponse> handleDuplicateTransactionException(
            DuplicateTransactionException ex, WebRequest request) {
        log.error("Duplicate transaction error: {}", ex.getMessage());
        
        UtilityErrorResponse errorResponse = UtilityErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Transaction")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle validation errors from @Valid annotations
     * Returns HTTP 400 with field-specific validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UtilityErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        UtilityErrorResponse errorResponse = UtilityErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request data. Please check the fields and try again.")
                .path(request.getDescription(false).replace("uri=", ""))
                .validationErrors(validationErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle generic exceptions
     * Returns HTTP 500 with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<UtilityErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        UtilityErrorResponse errorResponse = UtilityErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
