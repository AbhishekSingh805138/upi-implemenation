package com.upi.utility.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Custom validator for account identifiers
 * Provides provider-specific validation rules for different account types
 */
@Component
@Slf4j
public class AccountIdentifierValidator {

    private final Map<String, Predicate<String>> validationRules;

    public AccountIdentifierValidator() {
        this.validationRules = new HashMap<>();
        initializeValidationRules();
    }

    /**
     * Initialize provider-specific validation rules
     */
    private void initializeValidationRules() {
        // Mobile number validation (10 digits)
        validationRules.put("MOBILE", identifier -> 
            identifier != null && identifier.matches("^[0-9]{10}$"));

        // DTH subscriber ID validation (8-20 alphanumeric characters)
        validationRules.put("DTH", identifier -> 
            identifier != null && identifier.matches("^[a-zA-Z0-9]{8,20}$"));

        // Electricity consumer number validation (6-20 alphanumeric characters)
        validationRules.put("ELECTRICITY", identifier -> 
            identifier != null && identifier.matches("^[a-zA-Z0-9]{6,20}$"));

        // Credit card last 4 digits validation (exactly 4 digits)
        validationRules.put("CREDIT_CARD", identifier -> 
            identifier != null && identifier.matches("^[0-9]{4}$"));

        // Insurance policy number validation (6-20 alphanumeric characters)
        validationRules.put("INSURANCE", identifier -> 
            identifier != null && identifier.matches("^[a-zA-Z0-9]{6,20}$"));

        // Generic validation (alphanumeric, 3-30 characters)
        validationRules.put("GENERIC", identifier -> 
            identifier != null && identifier.matches("^[a-zA-Z0-9]{3,30}$"));
    }

    /**
     * Validate account identifier based on category
     * 
     * @param identifier The account identifier to validate
     * @param category The payment category (MOBILE, DTH, ELECTRICITY, etc.)
     * @return true if identifier is valid for the category, false otherwise
     */
    public boolean isValid(String identifier, String category) {
        if (identifier == null || identifier.trim().isEmpty()) {
            log.debug("Account identifier is null or empty");
            return false;
        }

        if (category == null || category.trim().isEmpty()) {
            log.debug("Category is null or empty, using generic validation");
            category = "GENERIC";
        }

        // Normalize category name
        String normalizedCategory = category.toUpperCase()
                .replace("_RECHARGE", "")
                .replace("_BILL", "")
                .replace("_PREMIUM", "");

        Predicate<String> validator = validationRules.getOrDefault(
                normalizedCategory, 
                validationRules.get("GENERIC"));

        boolean isValid = validator.test(identifier);
        log.debug("Account identifier validation for category {}: {}", 
                normalizedCategory, isValid);
        return isValid;
    }

    /**
     * Get validation error message for a category
     * 
     * @param category The payment category
     * @return Error message describing the expected format
     */
    public String getValidationMessage(String category) {
        if (category == null) {
            return "Account identifier must be alphanumeric and between 3-30 characters";
        }

        String normalizedCategory = category.toUpperCase()
                .replace("_RECHARGE", "")
                .replace("_BILL", "")
                .replace("_PREMIUM", "");

        return switch (normalizedCategory) {
            case "MOBILE" -> "Mobile number must be exactly 10 digits";
            case "DTH" -> "Subscriber ID must be alphanumeric and between 8-20 characters";
            case "ELECTRICITY" -> "Consumer number must be alphanumeric and between 6-20 characters";
            case "CREDIT_CARD" -> "Card last 4 digits must be exactly 4 digits";
            case "INSURANCE" -> "Policy number must be alphanumeric and between 6-20 characters";
            default -> "Account identifier must be alphanumeric and between 3-30 characters";
        };
    }
}
