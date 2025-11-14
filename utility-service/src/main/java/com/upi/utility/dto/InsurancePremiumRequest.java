package com.upi.utility.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePremiumRequest {

    @NotBlank(message = "UPI ID is required")
    private String upiId;

    @NotBlank(message = "Provider code is required")
    private String providerCode;

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}
