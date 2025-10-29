package com.upi.transaction.repository;

import com.upi.transaction.entity.Transaction;
import com.upi.transaction.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transaction by transaction reference
     */
    Optional<Transaction> findByTransactionRef(String transactionRef);
    
    /**
     * Check if transaction reference exists
     */
    boolean existsByTransactionRef(String transactionRef);
    
    /**
     * Find all transactions for a user (both sent and received)
     */
    @Query("SELECT t FROM Transaction t WHERE t.senderUpiId = :upiId OR t.receiverUpiId = :upiId ORDER BY t.createdAt DESC")
    List<Transaction> findAllTransactionsByUpiId(@Param("upiId") String upiId);
    
    /**
     * Find transactions sent by a user
     */
    List<Transaction> findBySenderUpiIdOrderByCreatedAtDesc(String senderUpiId);
    
    /**
     * Find transactions received by a user
     */
    List<Transaction> findByReceiverUpiIdOrderByCreatedAtDesc(String receiverUpiId);
    
    /**
     * Find transactions by status
     */
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    
    /**
     * Find transactions for a user within date range
     */
    @Query("SELECT t FROM Transaction t WHERE (t.senderUpiId = :upiId OR t.receiverUpiId = :upiId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsByUpiIdAndDateRange(
            @Param("upiId") String upiId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find transactions for a user by status
     */
    @Query("SELECT t FROM Transaction t WHERE (t.senderUpiId = :upiId OR t.receiverUpiId = :upiId) " +
           "AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsByUpiIdAndStatus(
            @Param("upiId") String upiId,
            @Param("status") TransactionStatus status);
    
    /**
     * Find transactions for a user by status and date range
     */
    @Query("SELECT t FROM Transaction t WHERE (t.senderUpiId = :upiId OR t.receiverUpiId = :upiId) " +
           "AND t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsByUpiIdStatusAndDateRange(
            @Param("upiId") String upiId,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count successful transactions for a user
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE (t.senderUpiId = :upiId OR t.receiverUpiId = :upiId) " +
           "AND t.status = 'SUCCESS'")
    Long countSuccessfulTransactionsByUpiId(@Param("upiId") String upiId);
    
    /**
     * Find recent transactions (last N transactions)
     */
    @Query("SELECT t FROM Transaction t WHERE (t.senderUpiId = :upiId OR t.receiverUpiId = :upiId) " +
           "ORDER BY t.createdAt DESC LIMIT :limit")
    List<Transaction> findRecentTransactionsByUpiId(@Param("upiId") String upiId, @Param("limit") int limit);
}