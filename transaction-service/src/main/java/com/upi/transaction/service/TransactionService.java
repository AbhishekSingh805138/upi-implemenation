package com.upi.transaction.service;

import com.upi.transaction.client.AccountServiceClient;
import com.upi.transaction.entity.Transaction;
import com.upi.transaction.enums.TransactionStatus;
import com.upi.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final Random random = new Random();
    
    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                             AccountServiceClient accountServiceClient) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
    }
    
    /**
     * Process money transfer between two UPI IDs
     */
    public Mono<Transaction> processTransfer(String senderUpiId, String receiverUpiId, 
                                           BigDecimal amount, String description) {
        
        // Validate input parameters
        if (senderUpiId == null || senderUpiId.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Sender UPI ID is required"));
        }
        
        if (receiverUpiId == null || receiverUpiId.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Receiver UPI ID is required"));
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Amount must be positive"));
        }
        
        if (senderUpiId.equals(receiverUpiId)) {
            return Mono.error(new IllegalArgumentException("Cannot transfer to same account"));
        }
        
        // Generate transaction reference
        String transactionRef = generateTransactionReference();
        
        // Create transaction with PENDING status
        Transaction transaction = new Transaction(
                senderUpiId, receiverUpiId, amount, description, 
                TransactionStatus.PENDING, transactionRef
        );
        
        // Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Process the transfer asynchronously
        return processTransferAsync(savedTransaction);
    }
    
    /**
     * Process transfer asynchronously with Account Service calls
     */
    private Mono<Transaction> processTransferAsync(Transaction transaction) {
        return Mono.fromCallable(() -> transaction)
                .flatMap(this::validateUpiIds)
                .flatMap(this::validateSenderBalance)
                .flatMap(this::executeTransfer)
                .onErrorResume(throwable -> handleTransferError(transaction, throwable));
    }
    
    /**
     * Validate both sender and receiver UPI IDs exist
     */
    private Mono<Transaction> validateUpiIds(Transaction transaction) {
        Mono<Boolean> senderValid = accountServiceClient.validateUpiId(transaction.getSenderUpiId());
        Mono<Boolean> receiverValid = accountServiceClient.validateUpiId(transaction.getReceiverUpiId());
        
        return Mono.zip(senderValid, receiverValid)
                .flatMap(tuple -> {
                    if (!tuple.getT1()) {
                        return Mono.error(new IllegalArgumentException("Invalid sender UPI ID: " + transaction.getSenderUpiId()));
                    }
                    if (!tuple.getT2()) {
                        return Mono.error(new IllegalArgumentException("Invalid receiver UPI ID: " + transaction.getReceiverUpiId()));
                    }
                    return Mono.just(transaction);
                });
    }
    
    /**
     * Validate sender has sufficient balance
     */
    private Mono<Transaction> validateSenderBalance(Transaction transaction) {
        return accountServiceClient.getBalance(transaction.getSenderUpiId())
                .flatMap(balanceResponse -> {
                    if (balanceResponse.getBalance().compareTo(transaction.getAmount()) < 0) {
                        return Mono.error(new IllegalArgumentException(
                                "Insufficient balance. Available: " + balanceResponse.getBalance() + 
                                ", Required: " + transaction.getAmount()));
                    }
                    return Mono.just(transaction);
                });
    }
    
    /**
     * Execute the actual transfer by debiting sender and crediting receiver
     */
    private Mono<Transaction> executeTransfer(Transaction transaction) {
        logger.info("Executing transfer: {} -> {}, Amount: {}", 
                   transaction.getSenderUpiId(), transaction.getReceiverUpiId(), transaction.getAmount());
        
        // Step 1: Debit from sender account
        Mono<AccountServiceClient.BalanceResponse> debitSender = 
                accountServiceClient.updateBalance(
                        transaction.getSenderUpiId(), 
                        transaction.getAmount().negate(), 
                        "DEBIT"
                );
        
        // Step 2: Credit to receiver account
        return debitSender
                .flatMap(debitResponse -> {
                    logger.debug("Successfully debited {} from sender {}", 
                               transaction.getAmount(), transaction.getSenderUpiId());
                    
                    return accountServiceClient.updateBalance(
                            transaction.getReceiverUpiId(), 
                            transaction.getAmount(), 
                            "CREDIT"
                    );
                })
                .flatMap(creditResponse -> {
                    logger.debug("Successfully credited {} to receiver {}", 
                               transaction.getAmount(), transaction.getReceiverUpiId());
                    
                    // Update transaction status to SUCCESS
                    transaction.setStatus(TransactionStatus.SUCCESS);
                    Transaction updatedTransaction = transactionRepository.save(transaction);
                    
                    logger.info("Transaction completed successfully: {}", transaction.getTransactionRef());
                    return Mono.just(updatedTransaction);
                })
                .onErrorResume(error -> {
                    logger.error("Error during transfer execution for transaction {}: {}", 
                               transaction.getTransactionRef(), error.getMessage());
                    
                    // If credit fails, we need to rollback the debit
                    return rollbackTransaction(transaction, error);
                });
    }
    
    /**
     * Rollback transaction by crediting back to sender if debit was successful
     */
    private Mono<Transaction> rollbackTransaction(Transaction transaction, Throwable originalError) {
        logger.warn("Rolling back transaction: {}", transaction.getTransactionRef());
        
        // Try to credit back to sender (rollback the debit)
        return accountServiceClient.updateBalance(
                transaction.getSenderUpiId(), 
                transaction.getAmount(), 
                "CREDIT"
        )
        .doOnSuccess(response -> logger.info("Successfully rolled back transaction: {}", 
                                           transaction.getTransactionRef()))
        .doOnError(rollbackError -> logger.error("Failed to rollback transaction {}: {}", 
                                                transaction.getTransactionRef(), rollbackError.getMessage()))
        .then(Mono.<Transaction>error(originalError)) // Return original error after rollback attempt
        .onErrorResume(rollbackError -> {
            // If rollback also fails, log and return original error
            logger.error("Critical: Rollback failed for transaction {}", 
                        transaction.getTransactionRef(), rollbackError);
            return Mono.error(originalError);
        });
    }
    
    /**
     * Handle transfer errors and update transaction status
     */
    private Mono<Transaction> handleTransferError(Transaction transaction, Throwable error) {
        logger.error("Transfer failed for transaction {}: {}", 
                    transaction.getTransactionRef(), error.getMessage());
        
        // Update transaction status to FAILED
        transaction.setStatus(TransactionStatus.FAILED);
        Transaction failedTransaction = transactionRepository.save(transaction);
        
        return Mono.error(error);
    }
    
    /**
     * Generate unique transaction reference
     */
    private String generateTransactionReference() {
        long timestamp = System.currentTimeMillis();
        int randomNum = random.nextInt(10000);
        return String.format("TXN%d%04d", timestamp, randomNum);
    }
    
    /**
     * Get transaction by ID
     */
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    /**
     * Get transaction history for a user (both sent and received)
     */
    public List<Transaction> getTransactionHistory(String upiId) {
        return transactionRepository.findAllTransactionsByUpiId(upiId);
    }
    
    /**
     * Get transaction history for a user within date range
     */
    public List<Transaction> getTransactionHistory(String upiId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findTransactionsByUpiIdAndDateRange(upiId, startDate, endDate);
    }
    
    /**
     * Get transaction by reference number
     */
    public Optional<Transaction> getTransactionByReference(String transactionRef) {
        return transactionRepository.findByTransactionRef(transactionRef);
    }
    
    /**
     * Get all transactions for a user (alias for getTransactionHistory)
     */
    public List<Transaction> getUserTransactions(String upiId) {
        return getTransactionHistory(upiId);
    }
    
    /**
     * Get transactions sent by a user
     */
    public List<Transaction> getSentTransactions(String upiId) {
        return transactionRepository.findBySenderUpiIdOrderByCreatedAtDesc(upiId);
    }
    
    /**
     * Get transactions received by a user
     */
    public List<Transaction> getReceivedTransactions(String upiId) {
        return transactionRepository.findByReceiverUpiIdOrderByCreatedAtDesc(upiId);
    }
    
    /**
     * Get transactions by status
     */
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * Get user transactions by date range (alias for getTransactionHistory with date range)
     */
    public List<Transaction> getUserTransactionsByDateRange(String upiId, LocalDateTime startDate, LocalDateTime endDate) {
        return getTransactionHistory(upiId, startDate, endDate);
    }
    
    /**
     * Get user transactions by status
     */
    public List<Transaction> getUserTransactionsByStatus(String upiId, TransactionStatus status) {
        return transactionRepository.findTransactionsByUpiIdAndStatus(upiId, status);
    }
    
    /**
     * Get recent transactions for a user
     */
    public List<Transaction> getRecentTransactions(String upiId, int limit) {
        return transactionRepository.findRecentTransactionsByUpiId(upiId, limit);
    }
    
    /**
     * Count successful transactions for a user
     */
    public Long countSuccessfulTransactions(String upiId) {
        return transactionRepository.countSuccessfulTransactionsByUpiId(upiId);
    }
}