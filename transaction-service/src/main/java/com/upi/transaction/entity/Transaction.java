package com.upi.transaction.entity;

import com.upi.transaction.enums.TransactionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_upi_id", nullable = false, length = 100)
    private String senderUpiId;
    
    @Column(name = "receiver_upi_id", nullable = false, length = 100)
    private String receiverUpiId;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;
    
    @Column(name = "transaction_ref", unique = true, nullable = false, length = 50)
    private String transactionRef;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public Transaction() {}
    
    // Constructor with required fields
    public Transaction(String senderUpiId, String receiverUpiId, BigDecimal amount, 
                      String description, TransactionStatus status, String transactionRef) {
        this.senderUpiId = senderUpiId;
        this.receiverUpiId = receiverUpiId;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.transactionRef = transactionRef;
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
        return "Transaction{" +
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