package com.upi.utility.client;

import com.upi.utility.dto.BalanceResponse;
import com.upi.utility.dto.BalanceUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@Slf4j
public class AccountServiceClient {

    private final WebClient webClient;
    private final String accountServiceBaseUrl;
    private final long timeout;
    private final int maxRetries;

    public AccountServiceClient(
            WebClient webClient,
            @Value("${account-service.base-url}") String accountServiceBaseUrl,
            @Value("${account-service.timeout:5000}") long timeout,
            @Value("${account-service.max-retries:3}") int maxRetries) {
        this.webClient = webClient;
        this.accountServiceBaseUrl = accountServiceBaseUrl;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    /**
     * Get account balance for a UPI ID
     */
    public Mono<BalanceResponse> getBalance(String upiId) {
        log.debug("Fetching balance for UPI ID: {}", upiId);
        
        return webClient.get()
                .uri(accountServiceBaseUrl + "/api/accounts/upi/{upiId}/balance", upiId)
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .doOnSuccess(response -> log.debug("Balance fetched successfully for {}: {}", 
                        upiId, response.getBalance()))
                .doOnError(error -> log.error("Error fetching balance for {}: {}", 
                        upiId, error.getMessage()));
    }

    /**
     * Debit amount from account
     */
    public Mono<BalanceResponse> debitAmount(String upiId, BigDecimal amount) {
        log.debug("Debiting amount {} from UPI ID: {}", amount, upiId);
        
        // Send positive amount with DEBIT operation - controller will negate it
        BalanceUpdateRequest request = new BalanceUpdateRequest(amount, "DEBIT");
        
        return webClient.put()
                .uri(accountServiceBaseUrl + "/api/accounts/upi/{upiId}/balance", upiId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .doOnSuccess(response -> log.debug("Amount debited successfully from {}", upiId))
                .doOnError(error -> log.error("Error debiting amount from {}: {}", 
                        upiId, error.getMessage()));
    }

    /**
     * Refund amount to account (used for rollback)
     */
    public Mono<BalanceResponse> refundAmount(String upiId, BigDecimal amount) {
        log.debug("Refunding amount {} to UPI ID: {}", amount, upiId);
        
        BalanceUpdateRequest request = new BalanceUpdateRequest(amount, "CREDIT");
        
        return webClient.put()
                .uri(accountServiceBaseUrl + "/api/accounts/upi/{upiId}/balance", upiId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .doOnSuccess(response -> log.debug("Amount refunded successfully to {}", upiId))
                .doOnError(error -> log.error("Error refunding amount to {}: {}", 
                        upiId, error.getMessage()));
    }

    /**
     * Validate if UPI ID exists
     */
    public Mono<Boolean> validateUpiId(String upiId) {
        log.debug("Validating UPI ID: {}", upiId);
        
        return webClient.get()
                .uri(accountServiceBaseUrl + "/api/accounts/validate/{upiId}", upiId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorReturn(false)
                .doOnSuccess(exists -> log.debug("UPI ID {} validation result: {}", upiId, exists));
    }

    /**
     * Check if exception is retryable
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            // Retry on 5xx server errors, but not on 4xx client errors
            return ex.getStatusCode().is5xxServerError();
        }
        // Retry on network errors
        return true;
    }
}
