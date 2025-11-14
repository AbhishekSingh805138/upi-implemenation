package com.upi.utility.service;

import com.upi.utility.dto.DTHRechargeRequest;
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
 * Service for handling DTH (Direct-to-Home) recharge operations
 * Implements subscriber ID validation, operator validation, and DTH recharge processing
 */
@Service
@Slf4j
public class DTHRechargeService {

    private static final String DTH_RECHARGE_CATEGORY = "DTH_RECHARGE";

    private final PaymentOrchestrationService paymentOrchestrationService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;
    private final RechargePlanRepository rechargePlanRepository;

    public DTHRechargeService(
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
     * Process DTH recharge request
     * Steps:
     * 1. Validate subscriber ID format
     * 2. Validate DTH operator code exists and is active
     * 3. Build utility payment request
     * 4. Call payment orchestration service
     * 
     * @param request DTH recharge request with UPI ID, subscriber ID, operator, and amount
     * @return Payment response with transaction details
     * @throws PaymentProcessingException if validation fails or payment processing fails
     */
    public UtilityPaymentResponse processDTHRecharge(DTHRechargeRequest request) {
        log.info("Processing DTH recharge for subscriber: {}, operator: {}, amount: {}", 
                maskSubscriberId(request.getSubscriberId()), 
                request.getOperatorCode(), 
                request.getAmount());

        // Step 1: Validate subscriber ID format
        validateSubscriberId(request.getSubscriberId());

        // Step 2: Validate DTH operator code exists and is active
        ServiceProvider operator = validateDTHOperator(request.getOperatorCode());

        // Step 3: Build utility payment request
        UtilityPaymentRequest paymentRequest = buildPaymentRequest(request, operator);

        // Step 4: Call payment orchestration service
        log.info("Initiating payment orchestration for DTH recharge");
        UtilityPaymentResponse response = paymentOrchestrationService.processUtilityPayment(paymentRequest);

        log.info("DTH recharge completed successfully. Transaction Ref: {}", 
                response.getTransactionRef());
        return response;
    }

    /**
     * Get all active DTH operators
     * Fetches all service providers in the DTH recharge category that are active
     * 
     * @return List of DTH operators as PaymentCategoryResponse
     */
    public List<PaymentCategoryResponse> getDTHOperators() {
        log.info("Fetching all active DTH operators");

        PaymentCategory dthCategory = paymentCategoryRepository
                .findByName(DTH_RECHARGE_CATEGORY)
                .orElseThrow(() -> new PaymentProcessingException(
                        "DTH recharge category not found"));

        List<ServiceProvider> operators = serviceProviderRepository
                .findByCategoryAndIsActiveTrue(dthCategory);

        log.info("Found {} active DTH operators", operators.size());

        return operators.stream()
                .map(this::mapToOperatorResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get DTH recharge plans for a specific operator and subscriber ID
     * Fetches all active recharge plans cached in the database for the given operator
     * 
     * @param operatorCode The DTH operator code (e.g., "TATA_SKY", "DISH_TV", "AIRTEL_DTH")
     * @param subscriberId The subscriber ID (optional, for personalized plans)
     * @return List of DTH recharge plans for the operator
     */
    public List<RechargePlanResponse> getDTHPlans(String operatorCode, String subscriberId) {
        log.info("Fetching DTH plans for operator: {}, subscriber: {}", 
                operatorCode, maskSubscriberId(subscriberId));

        ServiceProvider operator = validateDTHOperator(operatorCode);

        List<RechargePlan> plans = rechargePlanRepository
                .findByProviderAndIsActiveTrue(operator);

        log.info("Found {} active DTH plans for operator: {}", 
                plans.size(), operator.getProviderName());

        return plans.stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate subscriber ID format
     * Subscriber ID should not be empty and should be alphanumeric
     */
    private void validateSubscriberId(String subscriberId) {
        if (subscriberId == null || subscriberId.trim().isEmpty()) {
            throw new PaymentProcessingException("Subscriber ID is required");
        }

        // Basic validation - subscriber ID should be alphanumeric and between 8-20 characters
        if (subscriberId.length() < 8 || subscriberId.length() > 20) {
            throw new PaymentProcessingException(
                    "Invalid subscriber ID format. Subscriber ID must be between 8-20 characters");
        }

        if (!subscriberId.matches("^[a-zA-Z0-9]+$")) {
            throw new PaymentProcessingException(
                    "Invalid subscriber ID format. Subscriber ID must be alphanumeric");
        }

        log.debug("Subscriber ID validation passed: {}", maskSubscriberId(subscriberId));
    }

    /**
     * Validate DTH operator code exists and is active
     */
    private ServiceProvider validateDTHOperator(String operatorCode) {
        if (operatorCode == null || operatorCode.trim().isEmpty()) {
            throw new PaymentProcessingException("DTH operator code is required");
        }

        ServiceProvider operator = serviceProviderRepository
                .findByProviderCode(operatorCode)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid DTH operator code: " + operatorCode));

        if (!operator.getIsActive()) {
            throw new PaymentProcessingException(
                    "DTH operator is currently unavailable: " + operator.getProviderName());
        }

        // Verify operator belongs to DTH recharge category
        if (!DTH_RECHARGE_CATEGORY.equalsIgnoreCase(operator.getCategory().getName())) {
            throw new PaymentProcessingException(
                    "Provider " + operator.getProviderName() + " is not a DTH operator");
        }

        log.debug("DTH operator validation passed: {} ({})", 
                operator.getProviderName(), operatorCode);
        return operator;
    }

    /**
     * Build utility payment request from DTH recharge request
     */
    private UtilityPaymentRequest buildPaymentRequest(
            DTHRechargeRequest request, 
            ServiceProvider operator) {
        
        String description = String.format(
                "DTH recharge for subscriber %s via %s%s",
                maskSubscriberId(request.getSubscriberId()),
                operator.getProviderName(),
                request.getPlanCode() != null ? " (Plan: " + request.getPlanCode() + ")" : ""
        );

        return UtilityPaymentRequest.builder()
                .upiId(request.getUpiId())
                .providerCode(request.getOperatorCode())
                .categoryName(DTH_RECHARGE_CATEGORY)
                .accountIdentifier(request.getSubscriberId())
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
     * Mask subscriber ID for logging (show only last 4 characters)
     */
    private String maskSubscriberId(String subscriberId) {
        if (subscriberId == null || subscriberId.length() < 4) {
            return "****";
        }
        return "****" + subscriberId.substring(subscriberId.length() - 4);
    }
}
