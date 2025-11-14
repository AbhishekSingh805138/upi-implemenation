package com.upi.utility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCategoryResponse {

    private Long id;
    private String name;
    private String displayName;
    private String iconUrl;
    private Boolean isActive;
}
