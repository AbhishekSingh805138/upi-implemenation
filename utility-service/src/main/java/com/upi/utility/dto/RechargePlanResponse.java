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
public class RechargePlanResponse {

    private String planCode;
    private String planName;
    private BigDecimal amount;
    private Integer validityDays;
    private String description;
}
