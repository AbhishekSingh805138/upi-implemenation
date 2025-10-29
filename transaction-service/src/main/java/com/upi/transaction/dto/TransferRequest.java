package com.upi.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferRequest {
    
    @NotBlank(message = "Sender UPI ID is required")
    private String senderUpiId;
    
    @NotBlank(message = "Receiver UPI ID is required")
    private String receiverUpiId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String description;
    
    // Default constructor
    public TransferRequest() {}
    
    // Constructor
    public TransferRequest(String senderUpiId, String receiverUpiId, BigDecimal amount, String description) {
        this.senderUpiId = senderUpiId;
        this.receiverUpiId = receiverUpiId;
        this.amount = amount;
        this.description = description;
    }
    
    // Getters and Setters
    public String getSenderUpiId() {
        return senderUpiId;
    }
    
    public void setSenderUpiId(String senderUpiId) {
        this.senderUpiId = senderUpiId;
    }
    
    public String getReceiverUpiId() {
        return receiverUpiId;
    }
    
    public void setReceiverUpiId(String receiverUpiId) {
        this.receiverUpiId = receiverUpiId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "TransferRequest{" +
                "senderUpiId='" + senderUpiId + '\'' +
                ", receiverUpiId='" + receiverUpiId + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}