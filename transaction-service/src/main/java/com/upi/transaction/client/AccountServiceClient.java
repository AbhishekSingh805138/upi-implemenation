package com.upi.transaction.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class AccountServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceClient.class);
    
    private final WebClient webClient;
    private final String accountServiceBaseUrl;
    private final int timeout;
    private final int maxRetries;
    private final Duration retryDelay;
    
    public AccountServiceClient(WebClient.Builder webClientBuilder,
                               @Value("${account-service.base-url}") String accountServiceBaseUrl,
                               @Value("${account-service.timeout:5000}") int timeout,
                               @Value("${account-service.max-retries:3}") int maxRetries,
                               @Value("${account-service.retry-delay:1000}") long retryDelayMs) {
        this.accountServiceBaseUrl = accountServiceBaseUrl;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.retryDelay = Duration.ofMillis(retryDelayMs);
        this.webClient = webClientBuilder
                .baseUrl(accountServiceBaseUrl)
                .build();
        
        logger.info("AccountServiceClient initialized with baseUrl: {}, timeout: {}ms, maxRetries: {}", 
                   accountServiceBaseUrl, timeout, maxRetries);
    }
    
    /**
     * Get account balance by UPI ID with retry and error handling
     */
    public Mono<BalanceResponse> getBalance(String upiId) {
        logger.debug("Getting balance for UPI ID: {}", upiId);
        
        return webClient.get()
                .uri("/api/accounts/upi/{upiId}/balance", upiId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, 
                         response -> Mono.error(new AccountNotFoundException("Account not found for UPI ID: " + upiId)))
                .onStatus(status -> status.is4xxClientError(),
                         response -> Mono.error(new AccountServiceException("Client error for UPI ID: " + upiId + ", Status: " + response.statusCode())))
                .onStatus(status -> status.is5xxServerError(),
                         response -> Mono.error(new AccountServiceException("Server error for UPI ID: " + upiId + ", Status: " + response.statusCode())))
                .bodyToMono(BalanceResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(maxRetries, retryDelay)
                          .filter(this::isRetryableException)
                          .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                              new AccountServiceException("Failed to get balance after " + maxRetries + " retries for UPI ID: " + upiId, 
                                                         retrySignal.failure())))
                .doOnSuccess(response -> logger.debug("Successfully retrieved balance for UPI ID: {}", upiId))
                .doOnError(error -> logger.error("Failed to get balance for UPI ID: {}", upiId, error));
    }
    
    /**
     * Update account balance by UPI ID with retry and error handling
     */
    public Mono<BalanceResponse> updateBalance(String upiId, BigDecimal amount, String operation) {
        BalanceUpdateRequest request = new BalanceUpdateRequest(amount, operation);
        
        logger.debug("Updating balance for UPI ID: {}, amount: {}, operation: {}", upiId, amount, operation);
        
        return webClient.put()
                .uri("/api/accounts/upi/{upiId}/balance", upiId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, 
                         response -> Mono.error(new AccountNotFoundException("Account not found for UPI ID: " + upiId)))
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                         response -> Mono.error(new InsufficientBalanceException("Insufficient balance for UPI ID: " + upiId)))
                .onStatus(status -> status.is4xxClientError(),
                         response -> Mono.error(new AccountServiceException("Client error for UPI ID: " + upiId + ", Status: " + response.statusCode())))
                .onStatus(status -> status.is5xxServerError(),
                         response -> Mono.error(new AccountServiceException("Server error for UPI ID: " + upiId + ", Status: " + response.statusCode())))
                .bodyToMono(BalanceResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(maxRetries, retryDelay)
                          .filter(this::isRetryableException)
                          .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> 
                              new AccountServiceException("Failed to update balance after " + maxRetries + " retries for UPI ID: " + upiId, 
                                                         retrySignal.failure())))
                .doOnSuccess(response -> logger.debug("Successfully updated balance for UPI ID: {}", upiId))
                .doOnError(error -> logger.error("Failed to update balance for UPI ID: {}, amount: {}, operation: {}", 
                                                upiId, amount, operation, error));
    }
    
    /**
     * Validate UPI ID exists with retry and error handling
     */
    public Mono<Boolean> validateUpiId(String upiId) {
        logger.debug("Validating UPI ID: {}", upiId);
        
        return webClient.get()
                .uri("/api/accounts/validate/{upiId}", upiId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(maxRetries, retryDelay)
                          .filter(this::isRetryableException))
                .doOnSuccess(isValid -> logger.debug("UPI ID validation result for {}: {}", upiId, isValid))
                .doOnError(error -> logger.warn("Failed to validate UPI ID: {}", upiId, error))
                .onErrorReturn(false); // Return false if validation fails
    }
    
    /**
     * Determine if an exception is retryable
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException webClientException = (WebClientResponseException) throwable;
            HttpStatus status = HttpStatus.valueOf(webClientException.getStatusCode().value());
            
            // Retry on server errors and specific client errors
            return status.is5xxServerError() || 
                   status == HttpStatus.REQUEST_TIMEOUT ||
                   status == HttpStatus.TOO_MANY_REQUESTS;
        }
        
        // Retry on timeout and connection issues
        return throwable instanceof java.util.concurrent.TimeoutException ||
               throwable instanceof java.net.ConnectException ||
               throwable.getMessage() != null && 
               (throwable.getMessage().contains("Connection refused") ||
                throwable.getMessage().contains("timeout"));
    }
    
    // DTOs for Account Service communication
    public static class BalanceResponse {
        private BigDecimal balance;
        private String upiId;
        
        public BalanceResponse() {}
        
        public BalanceResponse(BigDecimal balance, String upiId) {
            this.balance = balance;
            this.upiId = upiId;
        }
        
        public BigDecimal getBalance() {
            return balance;
        }
        
        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
        
        public String getUpiId() {
            return upiId;
        }
        
        public void setUpiId(String upiId) {
            this.upiId = upiId;
        }
    }
    
    public static class BalanceUpdateRequest {
        private BigDecimal amount;
        private String operation;
        
        public BalanceUpdateRequest() {}
        
        public BalanceUpdateRequest(BigDecimal amount, String operation) {
            this.amount = amount;
            this.operation = operation;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
        
        public String getOperation() {
            return operation;
        }
        
        public void setOperation(String operation) {
            this.operation = operation;
        }
    }
    
    // Custom exceptions for Account Service communication
    public static class AccountServiceException extends RuntimeException {
        public AccountServiceException(String message) {
            super(message);
        }
        
        public AccountServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message) {
            super(message);
        }
    }
    
    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }
}