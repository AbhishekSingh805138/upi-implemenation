package com.upi.utility.validation;

import com.upi.utility.client.AccountServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Custom validator for UPI ID format and existence
 * Validates UPI ID format and checks if it exists in the account service
 */
@Component
@Slf4j
public class UpiIdValidator {

    private static final String UPI_ID_PATTERN = "^[a-zA-Z0-9.\\-_]{3,}@[a-zA-Z]{3,}$";
    
    private final AccountServiceClient accountServiceClient;

    public UpiIdValidator(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    /**
     * Validate UPI ID format
     * Format: username@provider (e.g., user@paytm, john.doe@ybl)
     * 
     * @param upiId The UPI ID to validate
     * @return true if format is valid, false otherwise
     */
    public boolean isValidFormat(String upiId) {
        if (upiId == null || upiId.trim().isEmpty()) {
            log.debug("UPI ID is null or empty");
            return false;
        }

        boolean isValid = upiId.matches(UPI_ID_PATTERN);
        log.debug("UPI ID format validation for {}: {}", maskUpiId(upiId), isValid);
        return isValid;
    }

    /**
     * Check if UPI ID exists in the account service
     * 
     * @param upiId The UPI ID to check
     * @return true if UPI ID exists, false otherwise
     */
    public boolean exists(String upiId) {
        try {
            Boolean exists = accountServiceClient.validateUpiId(upiId).block();
            log.debug("UPI ID existence check for {}: {}", maskUpiId(upiId), exists);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking UPI ID existence for {}: {}", maskUpiId(upiId), e.getMessage());
            return false;
        }
    }

    /**
     * Validate UPI ID format and existence
     * 
     * @param upiId The UPI ID to validate
     * @return true if format is valid and UPI ID exists, false otherwise
     */
    public boolean isValid(String upiId) {
        return isValidFormat(upiId) && exists(upiId);
    }

    /**
     * Mask UPI ID for logging (show only first 3 characters and provider)
     */
    private String maskUpiId(String upiId) {
        if (upiId == null || !upiId.contains("@")) {
            return "***";
        }
        String[] parts = upiId.split("@");
        String username = parts[0];
        String provider = parts.length > 1 ? parts[1] : "";
        
        if (username.length() <= 3) {
            return username + "@" + provider;
        }
        return username.substring(0, 3) + "***@" + provider;
    }
}
