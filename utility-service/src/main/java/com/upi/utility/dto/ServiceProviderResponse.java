package com.upi.utility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProviderResponse {

    private Long id;
    private String categoryName;
    private String categoryDisplayName;
    private String providerName;
    private String providerCode;
    private String apiEndpoint;
    private Boolean isActive;
}
