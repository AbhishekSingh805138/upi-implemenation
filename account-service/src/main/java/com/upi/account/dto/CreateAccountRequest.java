package com.upi.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateAccountRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance must be non-negative")
    private BigDecimal initialBalance;
    
    // Default constructor
    public CreateAccountRequest() {}
    
    // Constructor
    public CreateAccountRequest(Long userId, BigDecimal initialBalance) {
        this.userId = userId;
        this.initialBalance = initialBalance;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    
    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
    
    @Override
    public String toString() {
        return "CreateAccountRequest{" +
                "userId=" + userId +
                ", initialBalance=" + initialBalance +
                '}';
    }
}