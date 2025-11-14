package com.upi.utility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedBillerResponse {

    private Long id;
    private Long userId;
    private String categoryName;
    private String categoryDisplayName;
    private String providerCode;
    private String providerName;
    private String accountIdentifier;
    private String nickname;
    private String accountHolderName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
