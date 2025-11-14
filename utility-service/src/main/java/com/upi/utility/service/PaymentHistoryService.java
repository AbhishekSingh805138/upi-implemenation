package com.upi.utility.service;

import com.upi.utility.dto.PaymentHistoryResponse;
import com.upi.utility.dto.PaymentReceiptResponse;
import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.UtilityPayment;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.UtilityPaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing payment history and receipts
 * Provides functionality to view transaction history, filter payments, and generate receipts
 */
@Service
@Slf4j
public class PaymentHistoryService {

    private final UtilityPaymentRepository utilityPaymentRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;

    public PaymentHistoryService(
            UtilityPaymentRepository utilityPaymentRepository,
            PaymentCategoryRepository paymentCategoryRepository) {
        this.utilityPaymentRepository = utilityPaymentRepository;
        this.paymentCategoryRepository = paymentCategoryRepository;
    }

    /**
     * Get all utility payments for a user
     * Sorted by date in descending order (most recent first)
     * 
     * @param userId The user ID
     * @return List of payment history
     */
    public List<PaymentHistoryResponse> getUserPayments(Long userId) {
        log.info("Fetching payment history for user: {}", userId);

        List<UtilityPayment> payments = utilityPaymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        log.info("Found {} payments for user: {}", payments.size(), userId);
        return payments.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter payments by category
     * 
     * @param userId The user ID
     * @param categoryName The category name
     * @return List of payments for the category
     */
    public List<PaymentHistoryResponse> filterPaymentsByCategory(Long userId, String categoryName) {
        log.info("Filtering payments for user: {}, category: {}", userId, categoryName);

        PaymentCategory category = paymentCategoryRepository.findByName(categoryName)
                .orElseThrow(() -> new PaymentProcessingException("Invalid category: " + categoryName));

        List<UtilityPayment> payments = utilityPaymentRepository
                .findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
        
        log.info("Found {} payments for user: {}, category: {}", 
                payments.size(), userId, categoryName);
        return payments.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter payments by date range
     * 
     * @param userId The user ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of payments within the date range
     */
    public List<PaymentHistoryResponse> filterPaymentsByDateRange(
            Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Filtering payments for user: {}, date range: {} to {}", 
                userId, startDate, endDate);

        List<UtilityPayment> payments = utilityPaymentRepository
                .findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
        
        log.info("Found {} payments for user: {} in date range", payments.size(), userId);
        return payments.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get payment details by transaction ID
     * 
     * @param transactionId The payment transaction ID
     * @return Complete payment details
     * @throws PaymentProcessingException if payment not found
     */
    public PaymentHistoryResponse getPaymentDetails(Long transactionId) {
        log.info("Fetching payment details for transaction ID: {}", transactionId);

        UtilityPayment payment = utilityPaymentRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Payment not found with ID: " + transactionId));

        return mapToHistoryResponse(payment);
    }

    /**
     * Get payment details by transaction reference
     * 
     * @param transactionRef The transaction reference
     * @return Complete payment details
     * @throws PaymentProcessingException if payment not found
     */
    public PaymentHistoryResponse getPaymentDetailsByRef(String transactionRef) {
        log.info("Fetching payment details for transaction ref: {}", transactionRef);

        UtilityPayment payment = utilityPaymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Payment not found with transaction ref: " + transactionRef));

        return mapToHistoryResponse(payment);
    }

    /**
     * Generate payment receipt
     * Creates a detailed receipt with all transaction information
     * 
     * @param transactionId The payment transaction ID
     * @return Payment receipt with complete details
     * @throws PaymentProcessingException if payment not found
     */
    public PaymentReceiptResponse generateReceipt(Long transactionId) {
        log.info("Generating receipt for transaction ID: {}", transactionId);

        UtilityPayment payment = utilityPaymentRepository.findById(transactionId)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Payment not found with ID: " + transactionId));

        PaymentReceiptResponse receipt = mapToReceiptResponse(payment);
        
        log.info("Receipt generated successfully for transaction ref: {}", 
                payment.getTransactionRef());
        return receipt;
    }

    /**
     * Map UtilityPayment entity to PaymentHistoryResponse DTO
     */
    private PaymentHistoryResponse mapToHistoryResponse(UtilityPayment payment) {
        return PaymentHistoryResponse.builder()
                .id(payment.getId())
                .transactionRef(payment.getTransactionRef())
                .providerTransactionRef(payment.getProviderTransactionRef())
                .categoryName(payment.getCategory().getName())
                .categoryDisplayName(payment.getCategory().getDisplayName())
                .providerName(payment.getProvider().getProviderName())
                .accountIdentifier(payment.getAccountIdentifier())
                .amount(payment.getAmount())
                .status(payment.getPaymentStatus())
                .timestamp(payment.getCreatedAt())
                .paymentDetails(payment.getPaymentDetails())
                .build();
    }

    /**
     * Map UtilityPayment entity to PaymentReceiptResponse DTO
     * Includes all details needed for a complete receipt
     */
    private PaymentReceiptResponse mapToReceiptResponse(UtilityPayment payment) {
        Map<String, Object> additionalDetails = new HashMap<>();
        additionalDetails.put("paymentId", payment.getId());
        additionalDetails.put("completedAt", payment.getCompletedAt());
        additionalDetails.put("paymentDetails", payment.getPaymentDetails());
        
        return PaymentReceiptResponse.builder()
                .transactionRef(payment.getTransactionRef())
                .providerTransactionRef(payment.getProviderTransactionRef())
                .transactionDate(payment.getCreatedAt())
                .transactionTime(payment.getCreatedAt())
                .categoryName(payment.getCategory().getName())
                .categoryDisplayName(payment.getCategory().getDisplayName())
                .providerName(payment.getProvider().getProviderName())
                .accountIdentifier(payment.getAccountIdentifier())
                .amount(payment.getAmount())
                .status(payment.getPaymentStatus())
                .upiId(payment.getUpiId())
                .additionalDetails(additionalDetails)
                .build();
    }
}
