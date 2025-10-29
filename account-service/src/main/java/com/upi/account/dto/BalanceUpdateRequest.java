package com.upi.account.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BalanceUpdateRequest {
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Operation is required")
    private String operation; // DEBIT or CREDIT
    
    // Default constructor
    public BalanceUpdateRequest() {}
    
    // Constructor
    public BalanceUpdateRequest(BigDecimal amount, String operation) {
        this.amount = amount;
        this.operation = operation;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    @Override
    public String toString() {
        return "BalanceUpdateRequest{" +
                "amount=" + amount +
                ", operation='" + operation + '\'' +
                '}';
    }
}