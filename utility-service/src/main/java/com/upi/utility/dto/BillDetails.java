package com.upi.utility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDetails {

    private String consumerNumber;
    private String consumerName;
    private BigDecimal amountDue;
    private LocalDate dueDate;
    private String billingPeriod;
    private Map<String, Object> additionalDetails;
}
