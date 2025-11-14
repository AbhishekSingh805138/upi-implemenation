package com.upi.utility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityPaymentRecord {

    private String upiId;
    private String providerName;
    private BigDecimal amount;
    private String transactionRef;
    private String category;
}
