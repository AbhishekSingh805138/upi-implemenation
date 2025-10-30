package com.upi.account.service;

import com.upi.account.client.UserServiceClient;
import com.upi.account.entity.Account;
import com.upi.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class AccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    
    private final AccountRepository accountRepository;
    private final UserServiceClient userServiceClient;
    private final Random random = new Random();
    
    @Autowired
    public AccountService(AccountRepository accountRepository, UserServiceClient userServiceClient) {
        this.accountRepository = accountRepository;
        this.userServiceClient = userServiceClient;
    }
    
    /**
     * Create a new account for a user
     */
    public Account createAccount(Long userId, BigDecimal initialBalance) {
        logger.info("Creating account for user ID: {}", userId);
        
        // Validate user exists
        if (!userServiceClient.validateUserExists(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
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
        
        Account savedAccount = accountRepository.save(account);
        logger.info("Successfully created account with ID: {} for user ID: {}", savedAccount.getId(), userId);
        
        return savedAccount;
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
        
        // UPI ID format validation for phone-based IDs (10digits@upi or 10digits1@upi)
        if (!upiId.matches("^[0-9]{10}[0-9]*@upi$")) {
            return false;
        }
        
        // Check if UPI ID exists
        return accountRepository.existsByUpiId(upiId);
    }
    
    /**
     * Generate unique UPI ID for a user based on phone number
     */
    private String generateUniqueUpiId(Long userId) {
        logger.debug("Generating UPI ID for user ID: {}", userId);
        
        // Get user details to fetch phone number
        UserServiceClient.UserDetails userDetails = userServiceClient.getUserById(userId);
        if (userDetails == null || userDetails.getPhone() == null) {
            throw new IllegalArgumentException("Unable to fetch user phone number for UPI ID generation");
        }
        
        String phoneNumber = userDetails.getPhone();
        
        // Remove any non-digit characters and country code prefix
        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        
        // If phone starts with country code (like +91), remove it
        if (cleanPhone.startsWith("91") && cleanPhone.length() > 10) {
            cleanPhone = cleanPhone.substring(2);
        }
        
        // Ensure we have a 10-digit phone number
        if (cleanPhone.length() != 10) {
            throw new IllegalArgumentException("Invalid phone number format for UPI ID generation: " + phoneNumber);
        }
        
        String baseUpiId = cleanPhone;
        String upiId = baseUpiId + "@upi";
        int attempts = 0;
        
        // Check if UPI ID already exists (in case of duplicate phone numbers)
        while (accountRepository.existsByUpiId(upiId)) {
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique UPI ID after multiple attempts for phone: " + phoneNumber);
            }
            
            // If phone-based UPI ID exists, add a suffix
            int suffix = 1 + attempts;
            upiId = baseUpiId + suffix + "@upi";
            attempts++;
        }
        
        logger.info("Generated UPI ID: {} for user ID: {} with phone: {}", upiId, userId, phoneNumber);
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