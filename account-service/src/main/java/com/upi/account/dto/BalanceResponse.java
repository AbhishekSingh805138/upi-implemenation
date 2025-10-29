package com.upi.account.dto;

import java.math.BigDecimal;

public class BalanceResponse {
    
    private BigDecimal balance;
    private String upiId;
    
    // Default constructor
    public BalanceResponse() {}
    
    // Constructor
    public BalanceResponse(BigDecimal balance, String upiId) {
        this.balance = balance;
        this.upiId = upiId;
    }
    
    // Getters and Setters
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public String getUpiId() {
        return upiId;
    }
    
    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
    
    @Override
    public String toString() {
        return "BalanceResponse{" +
                "balance=" + balance +
                ", upiId='" + upiId + '\'' +
                '}';
    }
}