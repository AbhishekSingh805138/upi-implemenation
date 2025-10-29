package com.upi.account.controller;

import com.upi.account.dto.*;
import com.upi.account.entity.Account;
import com.upi.account.exception.AccountNotFoundException;
import com.upi.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    private final AccountService accountService;
    
    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    /**
     * Create a new account
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.getUserId(), request.getInitialBalance());
        AccountResponse response = new AccountResponse(account);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get account by user ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponse> getAccountByUserId(@PathVariable Long userId) {
        Optional<Account> accountOpt = accountService.getAccountByUserId(userId);
        if (accountOpt.isEmpty()) {
            throw new AccountNotFoundException("Account not found for user ID: " + userId);
        }
        
        AccountResponse response = new AccountResponse(accountOpt.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get account by UPI ID
     */
    @GetMapping("/upi/{upiId}")
    public ResponseEntity<AccountResponse> getAccountByUpiId(@PathVariable String upiId) {
        Optional<Account> accountOpt = accountService.getAccountByUpiId(upiId);
        if (accountOpt.isEmpty()) {
            throw new AccountNotFoundException("Account not found for UPI ID: " + upiId);
        }
        
        AccountResponse response = new AccountResponse(accountOpt.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get balance by UPI ID
     */
    @GetMapping("/upi/{upiId}/balance")
    public ResponseEntity<BalanceResponse> getBalanceByUpiId(@PathVariable String upiId) {
        Optional<BigDecimal> balanceOpt = accountService.getBalanceByUpiId(upiId);
        if (balanceOpt.isEmpty()) {
            throw new AccountNotFoundException("Account not found for UPI ID: " + upiId);
        }
        
        BalanceResponse response = new BalanceResponse(balanceOpt.get(), upiId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get balance by account ID
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalanceByAccountId(@PathVariable Long accountId) {
        Optional<BigDecimal> balanceOpt = accountService.getBalanceByAccountId(accountId);
        if (balanceOpt.isEmpty()) {
            throw new AccountNotFoundException("Account not found for ID: " + accountId);
        }
        
        // Get account to retrieve UPI ID for response
        Optional<Account> accountOpt = accountService.getAccountByUserId(accountId);
        String upiId = accountOpt.map(Account::getUpiId).orElse("unknown");
        
        BalanceResponse response = new BalanceResponse(balanceOpt.get(), upiId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update balance by UPI ID
     */
    @PutMapping("/upi/{upiId}/balance")
    public ResponseEntity<BalanceResponse> updateBalanceByUpiId(
            @PathVariable String upiId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        
        // Validate operation
        if (!"DEBIT".equalsIgnoreCase(request.getOperation()) && 
            !"CREDIT".equalsIgnoreCase(request.getOperation())) {
            throw new IllegalArgumentException("Operation must be either DEBIT or CREDIT");
        }
        
        // Calculate amount based on operation
        BigDecimal amount = request.getAmount();
        if ("DEBIT".equalsIgnoreCase(request.getOperation())) {
            amount = amount.negate();
        }
        
        // Update balance
        accountService.updateBalanceByUpiId(upiId, amount);
        
        // Get updated balance
        Optional<BigDecimal> updatedBalance = accountService.getBalanceByUpiId(upiId);
        if (updatedBalance.isEmpty()) {
            throw new AccountNotFoundException("Account not found for UPI ID: " + upiId);
        }
        
        BalanceResponse response = new BalanceResponse(updatedBalance.get(), upiId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update balance by account ID
     */
    @PutMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> updateBalanceByAccountId(
            @PathVariable Long accountId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        
        // Validate operation
        if (!"DEBIT".equalsIgnoreCase(request.getOperation()) && 
            !"CREDIT".equalsIgnoreCase(request.getOperation())) {
            throw new IllegalArgumentException("Operation must be either DEBIT or CREDIT");
        }
        
        // Calculate amount based on operation
        BigDecimal amount = request.getAmount();
        if ("DEBIT".equalsIgnoreCase(request.getOperation())) {
            amount = amount.negate();
        }
        
        // Update balance
        accountService.updateBalanceByAccountId(accountId, amount);
        
        // Get updated balance and UPI ID
        Optional<BigDecimal> updatedBalance = accountService.getBalanceByAccountId(accountId);
        if (updatedBalance.isEmpty()) {
            throw new AccountNotFoundException("Account not found for ID: " + accountId);
        }
        
        // Get UPI ID for response
        Optional<Account> accountOpt = accountService.getAccountByUserId(accountId);
        String upiId = accountOpt.map(Account::getUpiId).orElse("unknown");
        
        BalanceResponse response = new BalanceResponse(updatedBalance.get(), upiId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate UPI ID
     */
    @GetMapping("/validate/{upiId}")
    public ResponseEntity<Boolean> validateUpiId(@PathVariable String upiId) {
        boolean isValid = accountService.validateUpiId(upiId);
        return ResponseEntity.ok(isValid);
    }
}