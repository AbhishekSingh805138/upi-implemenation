package com.upi.utility.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedBillerRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    @NotBlank(message = "Provider code is required")
    private String providerCode;

    @NotBlank(message = "Account identifier is required")
    private String accountIdentifier;

    private String nickname;
    
    private String accountHolderName;
}
