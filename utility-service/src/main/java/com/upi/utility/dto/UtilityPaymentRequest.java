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
public class UtilityPaymentRequest {

    private String upiId;
    private String providerCode;
    private String categoryName;
    private String accountIdentifier;
    private BigDecimal amount;
    private String description;
}
