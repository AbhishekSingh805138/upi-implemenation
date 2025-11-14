package com.upi.utility.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "utility_payments", indexes = {
    @Index(name = "idx_user_payments", columnList = "userId,createdAt"),
    @Index(name = "idx_transaction_ref", columnList = "transactionRef")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String upiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PaymentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private ServiceProvider provider;

    @Column(nullable = false, length = 100)
    private String accountIdentifier;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionRef;

    @Column(length = 100)
    private String providerTransactionRef;

    @Column(columnDefinition = "TEXT")
    private String paymentDetails;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
