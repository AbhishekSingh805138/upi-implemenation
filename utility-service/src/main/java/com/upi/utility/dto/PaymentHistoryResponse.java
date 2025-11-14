package com.upi.utility.dto;

import com.upi.utility.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {

    private Long id;
    private String transactionRef;
    private String providerTransactionRef;
    private String categoryName;
    private String categoryDisplayName;
    private String providerName;
    private String accountIdentifier;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private String paymentDetails;
}
