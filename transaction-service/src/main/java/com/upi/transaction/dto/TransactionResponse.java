package com.upi.transaction.dto;

import com.upi.transaction.entity.Transaction;
import com.upi.transaction.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    
    private Long id;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private String transactionRef;
    private LocalDateTime createdAt;
    
    // Default constructor
    public TransactionResponse() {}
    
    // Constructor from Transaction entity
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.senderUpiId = transaction.getSenderUpiId();
        this.receiverUpiId = transaction.getReceiverUpiId();
        this.amount = transaction.getAmount();
        this.description = transaction.getDescription();
        this.status = transaction.getStatus();
        this.transactionRef = transaction.getTransactionRef();
        this.createdAt = transaction.getCreatedAt();
    }
    
    // Constructor with all fields
    public TransactionResponse(Long id, String senderUpiId, String receiverUpiId, 
                              BigDecimal amount, String description, TransactionStatus status,
                              String transactionRef, LocalDateTime createdAt) {
        this.id = id;
        this.senderUpiId = senderUpiId;
        this.receiverUpiId = receiverUpiId;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.transactionRef = transactionRef;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getTransactionRef() {
        return transactionRef;
    }
    
    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "TransactionResponse{" +
                "id=" + id +
                ", senderUpiId='" + senderUpiId + '\'' +
                ", receiverUpiId='" + receiverUpiId + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", transactionRef='" + transactionRef + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}