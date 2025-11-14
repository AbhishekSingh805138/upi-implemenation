package com.upi.utility.service;

import com.upi.utility.dto.InsurancePremiumRequest;
import com.upi.utility.dto.UtilityPaymentRequest;
import com.upi.utility.dto.UtilityPaymentResponse;
import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling insurance premium payment operations
 * Implements policy validation and premium payment processing
 */
@Service
@Slf4j
public class InsurancePremiumService {

    private static final String INSURANCE_PREMIUM_CATEGORY = "INSURANCE_PREMIUM";

    private final PaymentOrchestrationService paymentOrchestrationService;
    private final ServiceProviderRepository serviceProviderRepository;

    public InsurancePremiumService(
            PaymentOrchestrationService paymentOrchestrationService,
            ServiceProviderRepository serviceProviderRepository) {
        this.paymentOrchestrationService = paymentOrchestrationService;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    /**
     * Pay insurance premium
     * Steps:
     * 1. Validate policy number with insurance provider
     * 2. Validate insurance provider exists and is active
     * 3. Build utility payment request
     * 4. Call payment orchestration service
     * 5. Generate payment receipt with transaction details
     * 
     * @param request Insurance premium payment request
     * @return Payment response with transaction details and receipt
     * @throws PaymentProcessingException if validation fails or payment processing fails
     */
    public UtilityPaymentResponse payInsurancePremium(InsurancePremiumRequest request) {
        log.info("Processing insurance premium payment for provider: {}, policy: {}, amount: {}", 
                request.getProviderCode(), 
                maskPolicyNumber(request.getPolicyNumber()), 
                request.getAmount());

        // Step 1: Validate policy number
        validatePolicyNumber(request.getPolicyNumber());

        // Step 2: Validate insurance provider exists and is active
        ServiceProvider provider = validateInsuranceProvider(request.getProviderCode());

        // Step 3: Build utility payment request
        UtilityPaymentRequest paymentRequest = buildPaymentRequest(request, provider);

        // Step 4: Call payment orchestration service
        log.info("Initiating payment orchestration for insurance premium");
        UtilityPaymentResponse response = paymentOrchestrationService.processUtilityPayment(paymentRequest);

        // Step 5: Generate payment receipt with transaction details
        enhanceResponseWithReceipt(response, request, provider);

        log.info("Insurance premium payment completed successfully. Transaction Ref: {}", 
                response.getTransactionRef());
        return response;
    }

    /**
     * Validate policy number format
     * Policy number should be alphanumeric and between 8-20 characters
     */
    private void validatePolicyNumber(String policyNumber) {
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            throw new PaymentProcessingException("Policy number is required");
        }

        // Basic validation - policy number should be alphanumeric and between 8-20 characters
        if (policyNumber.length() < 8 || policyNumber.length() > 20) {
            throw new PaymentProcessingException(
                    "Invalid policy number format. Policy number must be between 8-20 characters");
        }

        if (!policyNumber.matches("^[a-zA-Z0-9]+$")) {
            throw new PaymentProcessingException(
                    "Invalid policy number format. Policy number must be alphanumeric");
        }

        log.debug("Policy number validation passed: {}", maskPolicyNumber(policyNumber));
    }

    /**
     * Validate insurance provider exists and is active
     */
    private ServiceProvider validateInsuranceProvider(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            throw new PaymentProcessingException("Insurance provider code is required");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderCode(providerCode)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid insurance provider code: " + providerCode));

        if (!provider.getIsActive()) {
            throw new PaymentProcessingException(
                    "Insurance provider is currently unavailable: " + provider.getProviderName());
        }

        // Verify provider belongs to insurance premium category
        if (!INSURANCE_PREMIUM_CATEGORY.equalsIgnoreCase(provider.getCategory().getName())) {
            throw new PaymentProcessingException(
                    "Provider " + provider.getProviderName() + " is not an insurance provider");
        }

        log.debug("Insurance provider validation passed: {} ({})", 
                provider.getProviderName(), providerCode);
        return provider;
    }

    /**
     * Build utility payment request from insurance premium request
     */
    private UtilityPaymentRequest buildPaymentRequest(
            InsurancePremiumRequest request, 
            ServiceProvider provider) {
        
        String description = String.format(
                "Insurance premium payment for policy %s via %s (Amount: %.2f)",
                maskPolicyNumber(request.getPolicyNumber()),
                provider.getProviderName(),
                request.getAmount()
        );

        return UtilityPaymentRequest.builder()
                .upiId(request.getUpiId())
                .providerCode(request.getProviderCode())
                .categoryName(INSURANCE_PREMIUM_CATEGORY)
                .accountIdentifier(request.getPolicyNumber())
                .amount(request.getAmount())
                .description(description)
                .build();
    }

    /**
     * Enhance response with payment receipt details
     * Includes policy number, premium amount, payment date, and transaction details
     */
    private void enhanceResponseWithReceipt(
            UtilityPaymentResponse response,
            InsurancePremiumRequest request,
            ServiceProvider provider) {
        
        Map<String, Object> receiptDetails = response.getReceiptDetails();
        if (receiptDetails == null) {
            receiptDetails = new HashMap<>();
        }

        // Add insurance-specific receipt details
        receiptDetails.put("policyNumber", maskPolicyNumber(request.getPolicyNumber()));
        receiptDetails.put("insuranceProvider", provider.getProviderName());
        receiptDetails.put("premiumAmount", request.getAmount());
        receiptDetails.put("paymentDate", LocalDateTime.now());
        receiptDetails.put("paymentType", "Insurance Premium");
        receiptDetails.put("receiptGenerated", true);

        response.setReceiptDetails(receiptDetails);
        
        log.debug("Payment receipt generated for policy: {}", maskPolicyNumber(request.getPolicyNumber()));
    }

    /**
     * Mask policy number for logging (show only last 4 characters)
     */
    private String maskPolicyNumber(String policyNumber) {
        if (policyNumber == null || policyNumber.length() < 4) {
            return "****";
        }
        return "****" + policyNumber.substring(policyNumber.length() - 4);
    }
}
