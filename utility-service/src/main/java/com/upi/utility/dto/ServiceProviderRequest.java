package com.upi.utility.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderRequest {

    @NotBlank(message = "Category name is required")
    private String categoryName;

    @NotBlank(message = "Provider name is required")
    private String providerName;

    @NotBlank(message = "Provider code is required")
    private String providerCode;

    @NotBlank(message = "API endpoint is required")
    private String apiEndpoint;

    private String apiKey;
}
