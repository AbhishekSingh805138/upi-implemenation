package com.upi.account.service;

import com.upi.account.entity.Account;
import com.upi.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final Random random = new Random();
    
    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    /**
     * Create a new account for a user
     */
    public Account createAccount(Long userId, BigDecimal initialBalance) {
        // Check if user already has an account
        if (accountRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User already has an account");
        }
        
        // Validate initial balance
        if (initialBalance != null && initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        
        // Generate unique UPI ID and account number
        String upiId = generateUniqueUpiId(userId);
        String accountNumber = generateUniqueAccountNumber();
        
        // Create and save account
        Account account = new Account(userId, upiId, accountNumber, 
                                    initialBalance != null ? initialBalance : BigDecimal.ZERO);
        
        return accountRepository.save(account);
    }
    
    /**
     * Get account by user ID
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }
    
    /**
     * Get account by UPI ID
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByUpiId(String upiId) {
        return accountRepository.findByUpiId(upiId);
    }
    
    /**
     * Get account by account number
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    /**
     * Get account balance by UPI ID
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getBalanceByUpiId(String upiId) {
        return accountRepository.getBalanceByUpiId(upiId);
    }
    
    /**
     * Get account balance by account ID
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getBalanceByAccountId(Long accountId) {
        return accountRepository.getBalanceByAccountId(accountId);
    }
    
    /**
     * Update account balance by UPI ID
     */
    public boolean updateBalanceByUpiId(String upiId, BigDecimal amount) {
        // Validate UPI ID exists
        Optional<Account> accountOpt = accountRepository.findByUpiId(upiId);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found for UPI ID: " + upiId);
        }
        
        Account account = accountOpt.get();
        BigDecimal newBalance = account.getBalance().add(amount);
        
        // Check for negative balance
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Current balance: " + 
                                             account.getBalance() + ", Requested amount: " + amount);
        }
        
        // Update balance
        account.setBalance(newBalance);
        accountRepository.save(account);
        return true;
    }
    
    /**
     * Update account balance by account ID
     */
    public boolean updateBalanceByAccountId(Long accountId, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found for ID: " + accountId);
        }
        
        Account account = accountOpt.get();
        BigDecimal newBalance = account.getBalance().add(amount);
        
        // Check for negative balance
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Current balance: " + 
                                             account.getBalance() + ", Requested amount: " + amount);
        }
        
        // Update balance
        account.setBalance(newBalance);
        accountRepository.save(account);
        return true;
    }
    
    /**
     * Validate UPI ID format and existence
     */
    @Transactional(readOnly = true)
    public boolean validateUpiId(String upiId) {
        if (upiId == null || upiId.trim().isEmpty()) {
            return false;
        }
        
        // Basic UPI ID format validation (user@bank)
        if (!upiId.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$")) {
            return false;
        }
        
        // Check if UPI ID exists
        return accountRepository.existsByUpiId(upiId);
    }
    
    /**
     * Generate unique UPI ID for a user
     */
    private String generateUniqueUpiId(Long userId) {
        String baseUpiId;
        String upiId;
        int attempts = 0;
        
        do {
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique UPI ID after multiple attempts");
            }
            
            // Generate UPI ID in format: user{userId}{random}@upi
            int randomSuffix = 1000 + random.nextInt(9000); // 4-digit random number
            baseUpiId = "user" + userId + randomSuffix;
            upiId = baseUpiId + "@upi";
            attempts++;
            
        } while (accountRepository.existsByUpiId(upiId));
        
        return upiId;
    }
    
    /**
     * Generate unique account number
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        int attempts = 0;
        
        do {
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique account number after multiple attempts");
            }
            
            // Generate 10-digit account number
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
            attempts++;
            
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }
}