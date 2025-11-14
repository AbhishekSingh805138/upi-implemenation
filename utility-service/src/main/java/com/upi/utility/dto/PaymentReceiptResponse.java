package com.upi.utility.dto;

import com.upi.utility.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceiptResponse {

    private String transactionRef;
    private String providerTransactionRef;
    private LocalDateTime transactionDate;
    private LocalDateTime transactionTime;
    private String categoryName;
    private String categoryDisplayName;
    private String providerName;
    private String accountIdentifier;
    private BigDecimal amount;
    private PaymentStatus status;
    private String upiId;
    private Map<String, Object> additionalDetails;
}
