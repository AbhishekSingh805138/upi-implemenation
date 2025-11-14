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
public class UtilityPaymentResponse {

    private String transactionRef;
    private String providerTransactionRef;
    private PaymentStatus status;
    private BigDecimal amount;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> receiptDetails;
}
