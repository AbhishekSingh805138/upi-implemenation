package com.upi.account.repository;

import com.upi.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Find account by user ID
     */
    Optional<Account> findByUserId(Long userId);
    
    /**
     * Find account by UPI ID
     */
    Optional<Account> findByUpiId(String upiId);
    
    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);
    
    /**
     * Check if UPI ID exists
     */
    boolean existsByUpiId(String upiId);
    
    /**
     * Check if account number exists
     */
    boolean existsByAccountNumber(String accountNumber);
    
    /**
     * Check if user already has an account
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Update account balance by UPI ID
     */
    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.upiId = :upiId")
    int updateBalanceByUpiId(@Param("upiId") String upiId, @Param("amount") BigDecimal amount);
    
    /**
     * Get balance by UPI ID
     */
    @Query("SELECT a.balance FROM Account a WHERE a.upiId = :upiId")
    Optional<BigDecimal> getBalanceByUpiId(@Param("upiId") String upiId);
    
    /**
     * Get balance by account ID
     */
    @Query("SELECT a.balance FROM Account a WHERE a.id = :accountId")
    Optional<BigDecimal> getBalanceByAccountId(@Param("accountId") Long accountId);
}