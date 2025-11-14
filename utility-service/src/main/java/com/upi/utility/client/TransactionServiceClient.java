package com.upi.utility.client;

import com.upi.utility.dto.TransactionResponse;
import com.upi.utility.dto.UtilityPaymentRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class TransactionServiceClient {

    private final WebClient webClient;
    private final String transactionServiceBaseUrl;
    private final long timeout;
    private final int maxRetries;

    public TransactionServiceClient(
            WebClient webClient,
            @Value("${transaction-service.base-url}") String transactionServiceBaseUrl,
            @Value("${transaction-service.timeout:5000}") long timeout,
            @Value("${transaction-service.max-retries:2}") int maxRetries) {
        this.webClient = webClient;
        this.transactionServiceBaseUrl = transactionServiceBaseUrl;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    /**
     * Record utility payment in transaction service
     */
    public Mono<TransactionResponse> recordUtilityPayment(UtilityPaymentRecord record) {
        log.debug("Recording utility payment for UPI ID: {}, Transaction Ref: {}", 
                record.getUpiId(), record.getTransactionRef());
        
        return webClient.post()
                .uri(transactionServiceBaseUrl + "/api/transactions/utility")
                .bodyValue(record)
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1)))
                .doOnSuccess(response -> log.debug("Utility payment recorded successfully: {}", 
                        response.getTransactionRef()))
                .doOnError(error -> log.error("Error recording utility payment: {}", 
                        error.getMessage()));
    }
}
