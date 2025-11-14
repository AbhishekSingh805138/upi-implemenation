package com.upi.utility.service;

import com.upi.utility.dto.BillDetails;
import com.upi.utility.dto.ElectricityBillPaymentRequest;
import com.upi.utility.dto.PaymentCategoryResponse;
import com.upi.utility.dto.UtilityPaymentRequest;
import com.upi.utility.dto.UtilityPaymentResponse;
import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.gateway.ServiceProviderGateway;
import com.upi.utility.gateway.ServiceProviderGatewayFactory;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for handling electricity bill payment operations
 * Implements consumer number validation, bill fetching, and electricity bill payment processing
 */
@Service
@Slf4j
public class ElectricityBillService {

    private static final String ELECTRICITY_BILL_CATEGORY = "ELECTRICITY_BILL";

    private final PaymentOrchestrationService paymentOrchestrationService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;
    private final ServiceProviderGatewayFactory gatewayFactory;

    public ElectricityBillService(
            PaymentOrchestrationService paymentOrchestrationService,
            ServiceProviderRepository serviceProviderRepository,
            PaymentCategoryRepository paymentCategoryRepository,
            ServiceProviderGatewayFactory gatewayFactory) {
        this.paymentOrchestrationService = paymentOrchestrationService;
        this.serviceProviderRepository = serviceProviderRepository;
        this.paymentCategoryRepository = paymentCategoryRepository;
        this.gatewayFactory = gatewayFactory;
    }

    /**
     * Fetch electricity bill details from provider
     * Steps:
     * 1. Validate consumer number format
     * 2. Validate electricity provider code exists and is active
     * 3. Fetch bill details from provider gateway
     * 4. Return bill details for user confirmation
     * 
     * @param providerCode The electricity provider code
     * @param consumerNumber The consumer number
     * @return Bill details including consumer name, amount due, due date, billing period
     * @throws PaymentProcessingException if validation fails or bill fetch fails
     */
    public BillDetails fetchBillDetails(String providerCode, String consumerNumber) {
        log.info("Fetching electricity bill for consumer: {}, provider: {}", 
                maskConsumerNumber(consumerNumber), providerCode);

        // Step 1: Validate consumer number format
        validateConsumerNumber(consumerNumber);

        // Step 2: Validate electricity provider code exists and is active
        ServiceProvider provider = validateElectricityProvider(providerCode);

        // Step 3: Fetch bill details from provider gateway
        log.info("Fetching bill details from provider gateway");
        ServiceProviderGateway gateway = gatewayFactory.getGateway(providerCode);
        BillDetails billDetails = gateway.fetchElectricityBill(consumerNumber, providerCode);

        if (billDetails == null) {
            throw new PaymentProcessingException(
                    "Unable to fetch bill details. Please verify consumer number and try again.");
        }

        log.info("Bill details fetched successfully for consumer: {}, amount due: {}", 
                maskConsumerNumber(consumerNumber), billDetails.getAmountDue());

        // Step 4: Return bill details for user confirmation
        return billDetails;
    }

    /**
     * Pay electricity bill
     * Steps:
     * 1. Validate consumer number format
     * 2. Validate electricity provider code exists and is active
     * 3. Validate bill details before payment (optional - can fetch bill first)
     * 4. Build utility payment request
     * 5. Call payment orchestration service
     * 
     * @param request Electricity bill payment request with UPI ID, provider, consumer number, and amount
     * @return Payment response with transaction details
     * @throws PaymentProcessingException if validation fails or payment processing fails
     */
    public UtilityPaymentResponse payElectricityBill(ElectricityBillPaymentRequest request) {
        log.info("Processing electricity bill payment for consumer: {}, provider: {}, amount: {}", 
                maskConsumerNumber(request.getConsumerNumber()), 
                request.getProviderCode(), 
                request.getAmount());

        // Step 1: Validate consumer number format
        validateConsumerNumber(request.getConsumerNumber());

        // Step 2: Validate electricity provider code exists and is active
        ServiceProvider provider = validateElectricityProvider(request.getProviderCode());

        // Step 3: Validate bill details before payment (optional)
        // In production, you might want to fetch and validate bill amount matches
        // For now, we'll proceed with the provided amount

        // Step 4: Build utility payment request
        UtilityPaymentRequest paymentRequest = buildPaymentRequest(request, provider);

        // Step 5: Call payment orchestration service
        log.info("Initiating payment orchestration for electricity bill");
        UtilityPaymentResponse response = paymentOrchestrationService.processUtilityPayment(paymentRequest);

        log.info("Electricity bill payment completed successfully. Transaction Ref: {}", 
                response.getTransactionRef());
        return response;
    }

    /**
     * Get all active electricity providers
     * Fetches all service providers in the electricity bill category that are active
     * 
     * @return List of electricity providers as PaymentCategoryResponse
     */
    public List<PaymentCategoryResponse> getElectricityProviders() {
        log.info("Fetching all active electricity providers");

        PaymentCategory electricityCategory = paymentCategoryRepository
                .findByName(ELECTRICITY_BILL_CATEGORY)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Electricity bill category not found"));

        List<ServiceProvider> providers = serviceProviderRepository
                .findByCategoryAndIsActiveTrue(electricityCategory);

        log.info("Found {} active electricity providers", providers.size());

        return providers.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate consumer number format
     * Consumer number should not be empty and should be alphanumeric
     */
    private void validateConsumerNumber(String consumerNumber) {
        if (consumerNumber == null || consumerNumber.trim().isEmpty()) {
            throw new PaymentProcessingException("Consumer number is required");
        }

        // Basic validation - consumer number should be alphanumeric and between 6-20 characters
        if (consumerNumber.length() < 6 || consumerNumber.length() > 20) {
            throw new PaymentProcessingException(
                    "Invalid consumer number format. Consumer number must be between 6-20 characters");
        }

        if (!consumerNumber.matches("^[a-zA-Z0-9]+$")) {
            throw new PaymentProcessingException(
                    "Invalid consumer number format. Consumer number must be alphanumeric");
        }

        log.debug("Consumer number validation passed: {}", maskConsumerNumber(consumerNumber));
    }

    /**
     * Validate electricity provider code exists and is active
     */
    private ServiceProvider validateElectricityProvider(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            throw new PaymentProcessingException("Electricity provider code is required");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderCode(providerCode)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid electricity provider code: " + providerCode));

        if (!provider.getIsActive()) {
            throw new PaymentProcessingException(
                    "Electricity provider is currently unavailable: " + provider.getProviderName());
        }

        // Verify provider belongs to electricity bill category
        if (!ELECTRICITY_BILL_CATEGORY.equalsIgnoreCase(provider.getCategory().getName())) {
            throw new PaymentProcessingException(
                    "Provider " + provider.getProviderName() + " is not an electricity provider");
        }

        log.debug("Electricity provider validation passed: {} ({})", 
                provider.getProviderName(), providerCode);
        return provider;
    }

    /**
     * Build utility payment request from electricity bill payment request
     */
    private UtilityPaymentRequest buildPaymentRequest(
            ElectricityBillPaymentRequest request, 
            ServiceProvider provider) {
        
        String description = String.format(
                "Electricity bill payment for consumer %s via %s%s",
                maskConsumerNumber(request.getConsumerNumber()),
                provider.getProviderName(),
                request.getBillingCycle() != null ? " (Billing Cycle: " + request.getBillingCycle() + ")" : ""
        );

        return UtilityPaymentRequest.builder()
                .upiId(request.getUpiId())
                .providerCode(request.getProviderCode())
                .categoryName(ELECTRICITY_BILL_CATEGORY)
                .accountIdentifier(request.getConsumerNumber())
                .amount(request.getAmount())
                .description(description)
                .build();
    }

    /**
     * Map ServiceProvider to PaymentCategoryResponse (provider response)
     */
    private PaymentCategoryResponse mapToProviderResponse(ServiceProvider provider) {
        return PaymentCategoryResponse.builder()
                .id(provider.getId())
                .name(provider.getProviderCode())
                .displayName(provider.getProviderName())
                .iconUrl(null) // Can be added later if needed
                .isActive(provider.getIsActive())
                .build();
    }

    /**
     * Mask consumer number for logging (show only last 4 characters)
     */
    private String maskConsumerNumber(String consumerNumber) {
        if (consumerNumber == null || consumerNumber.length() < 4) {
            return "****";
        }
        return "****" + consumerNumber.substring(consumerNumber.length() - 4);
    }
}
