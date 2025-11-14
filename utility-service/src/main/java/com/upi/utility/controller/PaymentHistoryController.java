package com.upi.utility.controller;

import com.upi.utility.dto.PaymentHistoryResponse;
import com.upi.utility.dto.PaymentReceiptResponse;
import com.upi.utility.service.PaymentHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for payment history operations
 * Provides endpoints to view payment history, filter payments, and generate receipts
 */
@RestController
@RequestMapping("/api/utilities/payments")
@Slf4j
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;

    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }

    /**
     * GET /api/utilities/payments/{userId}
     * Get user's utility payments
     * 
     * @param userId The user ID
     * @return List of payment history
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<PaymentHistoryResponse>> getUserPayments(
            @PathVariable Long userId) {
        log.info("GET /api/utilities/payments/{} - Fetching payment history", userId);
        List<PaymentHistoryResponse> history = paymentHistoryService.getUserPayments(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/utilities/payments/{userId}/{category}
     * Get payments by category
     * 
     * @param userId The user ID
     * @param category The category name
     * @return List of payments for the category
     */
    @GetMapping("/{userId}/{category}")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentsByCategory(
            @PathVariable Long userId,
            @PathVariable String category) {
        log.info("GET /api/utilities/payments/{}/{} - Filtering payments by category", 
                userId, category);
        List<PaymentHistoryResponse> history = paymentHistoryService
                .filterPaymentsByCategory(userId, category);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/utilities/payments/{userId}/daterange
     * Filter payments by date range
     * 
     * @param userId The user ID
     * @param startDate Start date (ISO format)
     * @param endDate End date (ISO format)
     * @return List of payments within date range
     */
    @GetMapping("/{userId}/daterange")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("GET /api/utilities/payments/{}/daterange - Filtering by date range", userId);
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        List<PaymentHistoryResponse> history = paymentHistoryService
                .filterPaymentsByDateRange(userId, start, end);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/utilities/payments/transaction/{id}
     * Get payment details by transaction ID
     * 
     * @param id The transaction ID
     * @return Payment details
     */
    @GetMapping("/transaction/{id}")
    public ResponseEntity<PaymentHistoryResponse> getPaymentDetails(
            @PathVariable Long id) {
        log.info("GET /api/utilities/payments/transaction/{} - Fetching payment details", id);
        PaymentHistoryResponse payment = paymentHistoryService.getPaymentDetails(id);
        return ResponseEntity.ok(payment);
    }

    /**
     * POST /api/utilities/payments/{id}/receipt
     * Generate payment receipt
     * 
     * @param id The transaction ID
     * @return Payment receipt
     */
    @PostMapping("/{id}/receipt")
    public ResponseEntity<PaymentReceiptResponse> generateReceipt(
            @PathVariable Long id) {
        log.info("POST /api/utilities/payments/{}/receipt - Generating receipt", id);
        PaymentReceiptResponse receipt = paymentHistoryService.generateReceipt(id);
        return ResponseEntity.ok(receipt);
    }
}
