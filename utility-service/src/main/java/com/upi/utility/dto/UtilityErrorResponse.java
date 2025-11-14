package com.upi.utility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilityErrorResponse {

    private String error;
    private String message;
    private int status;
    private String transactionRef;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> details;
    private Map<String, String> validationErrors;
}
