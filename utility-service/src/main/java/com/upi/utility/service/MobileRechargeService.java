package com.upi.utility.service;

import com.upi.utility.dto.MobileRechargeRequest;
import com.upi.utility.dto.PaymentCategoryResponse;
import com.upi.utility.dto.RechargePlanResponse;
import com.upi.utility.dto.UtilityPaymentRequest;
import com.upi.utility.dto.UtilityPaymentResponse;
import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.RechargePlan;
import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.RechargePlanRepository;
import com.upi.utility.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling mobile recharge operations
 * Implements mobile number validation, operator validation, and mobile recharge processing
 */
@Service
@Slf4j
public class MobileRechargeService {

    private static final String MOBILE_RECHARGE_CATEGORY = "MOBILE_RECHARGE";

    private final PaymentOrchestrationService paymentOrchestrationService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;
    private final RechargePlanRepository rechargePlanRepository;

    public MobileRechargeService(
            PaymentOrchestrationService paymentOrchestrationService,
            ServiceProviderRepository serviceProviderRepository,
            PaymentCategoryRepository paymentCategoryRepository,
            RechargePlanRepository rechargePlanRepository) {
        this.paymentOrchestrationService = paymentOrchestrationService;
        this.serviceProviderRepository = serviceProviderRepository;
        this.paymentCategoryRepository = paymentCategoryRepository;
        this.rechargePlanRepository = rechargePlanRepository;
    }

    /**
     * Process mobile recharge request
     * Steps:
     * 1. Validate mobile number format (10 digits)
     * 2. Validate operator code exists and is active
     * 3. Build utility payment request
     * 4. Call payment orchestration service
     * 
     * @param request Mobile recharge request with UPI ID, mobile number, operator, and amount
     * @return Payment response with transaction details
     * @throws PaymentProcessingException if validation fails or payment processing fails
     */
    public UtilityPaymentResponse processMobileRecharge(MobileRechargeRequest request) {
        log.info("Processing mobile recharge for number: {}, operator: {}, amount: {}", 
                maskMobileNumber(request.getMobileNumber()), 
                request.getOperatorCode(), 
                request.getAmount());

        // Step 1: Validate mobile number format (10 digits)
        validateMobileNumber(request.getMobileNumber());

        // Step 2: Validate operator code exists and is active
        ServiceProvider operator = validateMobileOperator(request.getOperatorCode());

        // Step 3: Build utility payment request
        UtilityPaymentRequest paymentRequest = buildPaymentRequest(request, operator);

        // Step 4: Call payment orchestration service
        log.info("Initiating payment orchestration for mobile recharge");
        UtilityPaymentResponse response = paymentOrchestrationService.processUtilityPayment(paymentRequest);

        log.info("Mobile recharge completed successfully. Transaction Ref: {}", 
                response.getTransactionRef());
        return response;
    }

    /**
     * Get all active mobile operators
     * Fetches all service providers in the mobile recharge category that are active
     * 
     * @return List of mobile operators as PaymentCategoryResponse
     */
    public List<PaymentCategoryResponse> getMobileOperators() {
        log.info("Fetching all active mobile operators");

        PaymentCategory mobileCategory = paymentCategoryRepository
                .findByName(MOBILE_RECHARGE_CATEGORY)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Mobile recharge category not found"));

        List<ServiceProvider> operators = serviceProviderRepository
                .findByCategoryAndIsActiveTrue(mobileCategory);

        log.info("Found {} active mobile operators", operators.size());

        return operators.stream()
                .map(this::mapToOperatorResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get mobile recharge plans for a specific operator
     * Fetches all active recharge plans cached in the database for the given operator
     * 
     * @param operatorCode The mobile operator code (e.g., "JIO", "AIRTEL", "VODAFONE")
     * @return List of mobile recharge plans for the operator
     */
    public List<RechargePlanResponse> getRechargePlans(String operatorCode) {
        log.info("Fetching recharge plans for operator: {}", operatorCode);

        ServiceProvider operator = validateMobileOperator(operatorCode);

        List<RechargePlan> plans = rechargePlanRepository
                .findByProviderAndIsActiveTrue(operator);

        log.info("Found {} active recharge plans for operator: {}", 
                plans.size(), operator.getProviderName());

        return plans.stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate mobile number format (10 digits)
     */
    private void validateMobileNumber(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            throw new PaymentProcessingException("Mobile number is required");
        }

        // Mobile number must be exactly 10 digits
        if (!mobileNumber.matches("^[0-9]{10}$")) {
            throw new PaymentProcessingException(
                    "Invalid mobile number format. Mobile number must be exactly 10 digits");
        }

        log.debug("Mobile number validation passed: {}", maskMobileNumber(mobileNumber));
    }

    /**
     * Validate mobile operator code exists and is active
     */
    private ServiceProvider validateMobileOperator(String operatorCode) {
        if (operatorCode == null || operatorCode.trim().isEmpty()) {
            throw new PaymentProcessingException("Mobile operator code is required");
        }

        ServiceProvider operator = serviceProviderRepository
                .findByProviderCode(operatorCode)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid mobile operator code: " + operatorCode));

        if (!operator.getIsActive()) {
            throw new PaymentProcessingException(
                    "Mobile operator is currently unavailable: " + operator.getProviderName());
        }

        // Verify operator belongs to mobile recharge category
        if (!MOBILE_RECHARGE_CATEGORY.equalsIgnoreCase(operator.getCategory().getName())) {
            throw new PaymentProcessingException(
                    "Provider " + operator.getProviderName() + " is not a mobile operator");
        }

        log.debug("Mobile operator validation passed: {} ({})", 
                operator.getProviderName(), operatorCode);
        return operator;
    }

    /**
     * Build utility payment request from mobile recharge request
     */
    private UtilityPaymentRequest buildPaymentRequest(
            MobileRechargeRequest request, 
            ServiceProvider operator) {
        
        String description = String.format(
                "Mobile recharge for %s via %s%s",
                maskMobileNumber(request.getMobileNumber()),
                operator.getProviderName(),
                request.getPlanCode() != null ? " (Plan: " + request.getPlanCode() + ")" : ""
        );

        return UtilityPaymentRequest.builder()
                .upiId(request.getUpiId())
                .providerCode(request.getOperatorCode())
                .categoryName(MOBILE_RECHARGE_CATEGORY)
                .accountIdentifier(request.getMobileNumber())
                .amount(request.getAmount())
                .description(description)
                .build();
    }

    /**
     * Map ServiceProvider to PaymentCategoryResponse (operator response)
     */
    private PaymentCategoryResponse mapToOperatorResponse(ServiceProvider provider) {
        return PaymentCategoryResponse.builder()
                .id(provider.getId())
                .name(provider.getProviderCode())
                .displayName(provider.getProviderName())
                .iconUrl(null) // Can be added later if needed
                .isActive(provider.getIsActive())
                .build();
    }

    /**
     * Map RechargePlan entity to RechargePlanResponse DTO
     */
    private RechargePlanResponse mapToPlanResponse(RechargePlan plan) {
        return RechargePlanResponse.builder()
                .planCode(plan.getPlanCode())
                .planName(plan.getPlanName())
                .amount(plan.getAmount())
                .validityDays(plan.getValidityDays())
                .description(plan.getDescription())
                .build();
    }

    /**
     * Mask mobile number for logging (show only last 4 digits)
     */
    private String maskMobileNumber(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.length() < 4) {
            return "****";
        }
        return "******" + mobileNumber.substring(mobileNumber.length() - 4);
    }
}
