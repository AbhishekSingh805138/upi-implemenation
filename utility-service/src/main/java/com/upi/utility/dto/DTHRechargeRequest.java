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
public class DTHRechargeRequest {

    @NotBlank(message = "UPI ID is required")
    private String upiId;

    @NotBlank(message = "Subscriber ID is required")
    private String subscriberId;

    @NotBlank(message = "Operator code is required")
    private String operatorCode;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String planCode;
}
