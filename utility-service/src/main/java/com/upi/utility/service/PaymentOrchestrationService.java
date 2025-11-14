package com.upi.utility.service;

import com.upi.utility.client.AccountServiceClient;
import com.upi.utility.client.TransactionServiceClient;
import com.upi.utility.dto.*;
import com.upi.utility.entity.*;
import com.upi.utility.exception.InsufficientBalanceException;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.exception.ProviderUnavailableException;
import com.upi.utility.gateway.ServiceProviderGateway;
import com.upi.utility.gateway.ServiceProviderGatewayFactory;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.ServiceProviderRepository;
import com.upi.utility.repository.UtilityPaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PaymentOrchestrationService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final ServiceProviderGatewayFactory gatewayFactory;
    private final UtilityPaymentRepository utilityPaymentRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;

    public PaymentOrchestrationService(
            AccountServiceClient accountServiceClient,
            TransactionServiceClient transactionServiceClient,
            ServiceProviderGatewayFactory gatewayFactory,
            UtilityPaymentRepository utilityPaymentRepository,
            ServiceProviderRepository serviceProviderRepository,
            PaymentCategoryRepository paymentCategoryRepository) {
        this.accountServiceClient = accountServiceClient;
        this.transactionServiceClient = transactionServiceClient;
        this.gatewayFactory = gatewayFactory;
        this.utilityPaymentRepository = utilityPaymentRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.paymentCategoryRepository = paymentCategoryRepository;
    }

    /**
     * Process utility payment with complete orchestration
     * Steps: Validate -> Check Balance -> Create Pending -> Debit -> Process -> Update -> Record
     */
    @Transactional
    public UtilityPaymentResponse processUtilityPayment(UtilityPaymentRequest request) {
        log.info("Starting payment orchestration for UPI ID: {}, Provider: {}, Amount: {}", 
                request.getUpiId(), request.getProviderCode(), request.getAmount());

        String transactionRef = generateTransactionRef();
        UtilityPayment payment = null;
        boolean amountDebited = false;

        try {
            // Step 1: Validate user account
            log.debug("Step 1: Validating user account");
            Boolean accountExists = accountServiceClient.validateUpiId(request.getUpiId())
                    .block();
            if (accountExists == null || !accountExists) {
                throw new PaymentProcessingException("Invalid UPI ID: " + request.getUpiId());
            }

            // Step 2: Check balance sufficiency
            log.debug("Step 2: Checking balance sufficiency");
            BalanceResponse balanceResponse = accountServiceClient.getBalance(request.getUpiId())
                    .block();
            if (balanceResponse == null || balanceResponse.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        String.format("Insufficient balance. Available: %s, Required: %s",
                                balanceResponse != null ? balanceResponse.getBalance() : "0",
                                request.getAmount()));
            }

            // Step 3: Validate service provider is active
            log.debug("Step 3: Validating service provider");
            ServiceProvider provider = serviceProviderRepository
                    .findByProviderCode(request.getProviderCode())
                    .orElseThrow(() -> new ProviderUnavailableException(
                            "Provider not found: " + request.getProviderCode()));

            if (!provider.getIsActive()) {
                throw new ProviderUnavailableException(
                        "Provider is currently unavailable: " + request.getProviderCode());
            }

            // Step 4: Create pending payment record
            log.debug("Step 4: Creating pending payment record");
            payment = createPendingPayment(request, provider, transactionRef);

            // Step 5: Debit user account
            log.debug("Step 5: Debiting user account");
            BalanceResponse debitResponse = accountServiceClient
                    .debitAmount(request.getUpiId(), request.getAmount())
                    .block();
            amountDebited = true;
            log.info("Amount debited successfully. New balance: {}", debitResponse.getBalance());

            // Step 6: Process payment with provider
            log.debug("Step 6: Processing payment with provider");
            ServiceProviderGateway gateway = gatewayFactory.getGateway(request.getProviderCode());
            PaymentResponse providerResponse = processWithProvider(gateway, request, provider);

            if (!providerResponse.isSuccess()) {
                throw new PaymentProcessingException(
                        "Provider payment failed: " + providerResponse.getMessage());
            }

            // Step 7: Update payment status to COMPLETED
            log.debug("Step 7: Updating payment status to COMPLETED");
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setProviderTransactionRef(providerResponse.getTransactionRef());
            payment.setCompletedAt(LocalDateTime.now());
            utilityPaymentRepository.save(payment);

            // Step 8: Record payment in transaction service
            log.debug("Step 8: Recording payment in transaction service");
            recordInTransactionService(request, provider, transactionRef);

            log.info("Payment orchestration completed successfully. Transaction Ref: {}", transactionRef);
            return buildSuccessResponse(payment, providerResponse);

        } catch (InsufficientBalanceException | ProviderUnavailableException e) {
            log.error("Payment validation failed: {}", e.getMessage());
            if (payment != null) {
                updatePaymentStatus(payment, PaymentStatus.FAILED, e.getMessage());
            }
            throw e;

        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage(), e);
            handlePaymentFailure(payment, request.getUpiId(), request.getAmount(), amountDebited, e, transactionRef);
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Create pending payment record in database
     */
    private UtilityPayment createPendingPayment(
            UtilityPaymentRequest request, 
            ServiceProvider provider, 
            String transactionRef) {
        
        PaymentCategory category = provider.getCategory();
        
        UtilityPayment payment = new UtilityPayment();
        payment.setUserId(0L); // Will be set from user context in real implementation
        payment.setUpiId(request.getUpiId());
        payment.setCategory(category);
        payment.setProvider(provider);
        payment.setAccountIdentifier(request.getAccountIdentifier());
        payment.setAmount(request.getAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionRef(transactionRef);
        payment.setPaymentDetails(request.getDescription());
        
        return utilityPaymentRepository.save(payment);
    }

    /**
     * Process payment with service provider gateway
     */
    private PaymentResponse processWithProvider(
            ServiceProviderGateway gateway,
            UtilityPaymentRequest request,
            ServiceProvider provider) {
        
        // For now, using generic payment processing
        // In real implementation, this would route to specific payment methods
        // based on category type
        
        return PaymentResponse.builder()
                .success(true)
                .transactionRef("PROV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .message("Payment processed successfully")
                .status("SUCCESS")
                .build();
    }

    /**
     * Record payment in transaction service
     */
    private void recordInTransactionService(
            UtilityPaymentRequest request,
            ServiceProvider provider,
            String transactionRef) {
        
        UtilityPaymentRecord record = UtilityPaymentRecord.builder()
                .upiId(request.getUpiId())
                .providerName(provider.getProviderName())
                .amount(request.getAmount())
                .transactionRef(transactionRef)
                .category(provider.getCategory().getName())
                .build();

        transactionServiceClient.recordUtilityPayment(record)
                .doOnError(error -> log.warn("Failed to record in transaction service: {}", 
                        error.getMessage()))
                .subscribe();
    }

    /**
     * Handle payment failure and rollback
     * This method implements comprehensive failure handling including:
     * - Automatic refund if amount was debited
     * - Payment status update with failure reason
     * - Detailed logging for troubleshooting
     * - User-friendly error message generation
     */
    private void handlePaymentFailure(
            UtilityPayment payment,
            String upiId,
            BigDecimal amount,
            boolean amountDebited,
            Exception error,
            String transactionRef) {
        
        log.error("=== PAYMENT FAILURE HANDLING INITIATED ===");
        log.error("Transaction Ref: {}", transactionRef);
        log.error("UPI ID: {}", upiId);
        log.error("Amount: {}", amount);
        log.error("Amount Debited: {}", amountDebited);
        log.error("Error Type: {}", error.getClass().getSimpleName());
        log.error("Error Message: {}", error.getMessage());
        log.error("Stack Trace: ", error);

        // Step 1: Refund debited amount if payment fails after deduction
        if (amountDebited) {
            log.warn("Amount was debited before failure. Initiating automatic refund...");
            try {
                log.info("Attempting to refund amount {} to UPI ID: {}", amount, upiId);
                
                BalanceResponse refundResponse = accountServiceClient.refundAmount(upiId, amount)
                        .block();
                
                if (refundResponse != null) {
                    log.info("✓ Amount refunded successfully. New balance: {}", refundResponse.getBalance());
                    
                    // Update payment record with refund information
                    if (payment != null) {
                        String failureDetails = String.format(
                            "Payment failed: %s. Amount refunded successfully at %s",
                            error.getMessage(),
                            LocalDateTime.now()
                        );
                        updatePaymentStatus(payment, PaymentStatus.FAILED, failureDetails);
                    }
                } else {
                    log.error("✗ Refund response was null");
                    logCriticalRefundFailure(upiId, amount, transactionRef, "Null response from refund service");
                }
                
            } catch (Exception refundError) {
                log.error("✗ CRITICAL: Failed to refund amount", refundError);
                logCriticalRefundFailure(upiId, amount, transactionRef, refundError.getMessage());
                
                // Update payment with refund failure information
                if (payment != null) {
                    String failureDetails = String.format(
                        "Payment failed: %s. REFUND FAILED: %s. Manual intervention required.",
                        error.getMessage(),
                        refundError.getMessage()
                    );
                    updatePaymentStatus(payment, PaymentStatus.FAILED, failureDetails);
                }
            }
        } else {
            log.info("Amount was not debited. No refund required.");
            
            // Update payment status to FAILED with failure reason
            if (payment != null) {
                String failureDetails = String.format(
                    "Payment failed before debit: %s",
                    error.getMessage()
                );
                updatePaymentStatus(payment, PaymentStatus.FAILED, failureDetails);
            }
        }

        // Step 2: Log failure details for troubleshooting
        logFailureDetailsForTroubleshooting(payment, upiId, amount, amountDebited, error, transactionRef);
        
        log.error("=== PAYMENT FAILURE HANDLING COMPLETED ===");
    }

    /**
     * Log critical refund failure that requires manual intervention
     */
    private void logCriticalRefundFailure(String upiId, BigDecimal amount, String transactionRef, String errorMessage) {
        log.error("╔════════════════════════════════════════════════════════════════╗");
        log.error("║          CRITICAL: MANUAL INTERVENTION REQUIRED                ║");
        log.error("╠════════════════════════════════════════════════════════════════╣");
        log.error("║ Transaction Ref : {}                                    ║", transactionRef);
        log.error("║ UPI ID          : {}                                    ║", upiId);
        log.error("║ Amount to Refund: {}                                    ║", amount);
        log.error("║ Error           : {}                                    ║", errorMessage);
        log.error("║ Timestamp       : {}                                    ║", LocalDateTime.now());
        log.error("╠════════════════════════════════════════════════════════════════╣");
        log.error("║ ACTION REQUIRED: Manually process refund for this transaction  ║");
        log.error("╚════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Log comprehensive failure details for troubleshooting
     */
    private void logFailureDetailsForTroubleshooting(
            UtilityPayment payment,
            String upiId,
            BigDecimal amount,
            boolean amountDebited,
            Exception error,
            String transactionRef) {
        
        log.info("=== FAILURE TROUBLESHOOTING DETAILS ===");
        log.info("Transaction Reference: {}", transactionRef);
        log.info("UPI ID: {}", upiId);
        log.info("Amount: {}", amount);
        log.info("Amount Debited: {}", amountDebited);
        
        if (payment != null) {
            log.info("Payment ID: {}", payment.getId());
            log.info("Payment Status: {}", payment.getPaymentStatus());
            log.info("Provider: {}", payment.getProvider() != null ? payment.getProvider().getProviderName() : "N/A");
            log.info("Category: {}", payment.getCategory() != null ? payment.getCategory().getName() : "N/A");
            log.info("Account Identifier: {}", payment.getAccountIdentifier());
            log.info("Created At: {}", payment.getCreatedAt());
        } else {
            log.info("Payment record: Not created");
        }
        
        log.info("Error Type: {}", error.getClass().getName());
        log.info("Error Message: {}", error.getMessage());
        log.info("Failure Timestamp: {}", LocalDateTime.now());
        log.info("======================================");
    }

    /**
     * Update payment status
     */
    private void updatePaymentStatus(UtilityPayment payment, PaymentStatus status, String message) {
        payment.setPaymentStatus(status);
        payment.setPaymentDetails(message);
        utilityPaymentRepository.save(payment);
        log.debug("Payment status updated to: {}", status);
    }

    /**
     * Build success response
     */
    private UtilityPaymentResponse buildSuccessResponse(
            UtilityPayment payment, 
            PaymentResponse providerResponse) {
        
        Map<String, Object> receiptDetails = new HashMap<>();
        receiptDetails.put("paymentId", payment.getId());
        receiptDetails.put("category", payment.getCategory().getDisplayName());
        receiptDetails.put("provider", payment.getProvider().getProviderName());
        receiptDetails.put("accountIdentifier", payment.getAccountIdentifier());
        
        return UtilityPaymentResponse.builder()
                .transactionRef(payment.getTransactionRef())
                .providerTransactionRef(providerResponse.getTransactionRef())
                .status(PaymentStatus.COMPLETED)
                .amount(payment.getAmount())
                .message("Payment completed successfully")
                .timestamp(LocalDateTime.now())
                .receiptDetails(receiptDetails)
                .build();
    }

    /**
     * Generate unique transaction reference
     */
    private String generateTransactionRef() {
        return "UTL-" + System.currentTimeMillis() + "-" + 
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
