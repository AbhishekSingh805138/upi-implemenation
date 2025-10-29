package com.upi.transaction.controller;

import com.upi.transaction.dto.ErrorResponse;
import com.upi.transaction.dto.TransactionResponse;
import com.upi.transaction.dto.TransferRequest;
import com.upi.transaction.entity.Transaction;
import com.upi.transaction.enums.TransactionStatus;
import com.upi.transaction.exception.TransactionNotFoundException;
import com.upi.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    /**
     * Process money transfer
     */
    @PostMapping("/transfer")
    public Mono<ResponseEntity<TransactionResponse>> processTransfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.processTransfer(
                        request.getSenderUpiId(),
                        request.getReceiverUpiId(),
                        request.getAmount(),
                        request.getDescription()
                )
                .map(transaction -> {
                    TransactionResponse response = new TransactionResponse(transaction);
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                })
                .onErrorResume(throwable -> {
                    // Return error response for failed transfers
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).<TransactionResponse>build());
                });
    }
    
    /**
     * Get transaction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        Optional<Transaction> transactionOpt = transactionService.getTransactionById(id);
        if (transactionOpt.isEmpty()) {
            throw new TransactionNotFoundException("Transaction not found with ID: " + id);
        }
        
        TransactionResponse response = new TransactionResponse(transactionOpt.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get transaction by reference
     */
    @GetMapping("/ref/{transactionRef}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String transactionRef) {
        Optional<Transaction> transactionOpt = transactionService.getTransactionByReference(transactionRef);
        if (transactionOpt.isEmpty()) {
            throw new TransactionNotFoundException("Transaction not found with reference: " + transactionRef);
        }
        
        TransactionResponse response = new TransactionResponse(transactionOpt.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all transactions for a user (both sent and received)
     */
    @GetMapping("/user/{upiId}")
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(@PathVariable String upiId) {
        List<Transaction> transactions = transactionService.getUserTransactions(upiId);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get transactions sent by a user
     */
    @GetMapping("/user/{upiId}/sent")
    public ResponseEntity<List<TransactionResponse>> getSentTransactions(@PathVariable String upiId) {
        List<Transaction> transactions = transactionService.getSentTransactions(upiId);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get transactions received by a user
     */
    @GetMapping("/user/{upiId}/received")
    public ResponseEntity<List<TransactionResponse>> getReceivedTransactions(@PathVariable String upiId) {
        List<Transaction> transactions = transactionService.getReceivedTransactions(upiId);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get transactions by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        List<Transaction> transactions = transactionService.getTransactionsByStatus(status);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get filtered transactions for a user
     */
    @GetMapping("/user/{upiId}/filter")
    public ResponseEntity<List<TransactionResponse>> getFilteredTransactions(
            @PathVariable String upiId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "50") int limit) {
        
        List<Transaction> transactions;
        
        if (status != null && startDate != null && endDate != null) {
            // Filter by status and date range
            transactions = transactionService.getUserTransactionsByDateRange(upiId, startDate, endDate)
                    .stream()
                    .filter(t -> t.getStatus() == status)
                    .limit(limit)
                    .collect(Collectors.toList());
        } else if (status != null) {
            // Filter by status only
            transactions = transactionService.getUserTransactionsByStatus(upiId, status)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            // Filter by date range only
            transactions = transactionService.getUserTransactionsByDateRange(upiId, startDate, endDate)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } else {
            // No filters, get recent transactions
            transactions = transactionService.getRecentTransactions(upiId, limit);
        }
        
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get recent transactions for a user
     */
    @GetMapping("/user/{upiId}/recent")
    public ResponseEntity<List<TransactionResponse>> getRecentTransactions(
            @PathVariable String upiId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Transaction> transactions = transactionService.getRecentTransactions(upiId, limit);
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get transaction count for a user
     */
    @GetMapping("/user/{upiId}/count")
    public ResponseEntity<Long> getTransactionCount(@PathVariable String upiId) {
        Long count = transactionService.countSuccessfulTransactions(upiId);
        return ResponseEntity.ok(count);
    }
}