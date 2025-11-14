package com.upi.utility.service;

import com.upi.utility.dto.CreditCardPaymentRequest;
import com.upi.utility.dto.PaymentCategoryResponse;
import com.upi.utility.dto.UtilityPaymentRequest;
import com.upi.utility.dto.UtilityPaymentResponse;
import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling credit card bill payment operations
 * Implements card validation and payment processing for credit card bills
 */
@Service
@Slf4j
public class CreditCardBillService {

    private static final String CREDIT_CARD_BILL_CATEGORY = "CREDIT_CARD_BILL";

    private final PaymentOrchestrationService paymentOrchestrationService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;

    public CreditCardBillService(
            PaymentOrchestrationService paymentOrchestrationService,
            ServiceProviderRepository serviceProviderRepository,
            PaymentCategoryRepository paymentCategoryRepository) {
        this.paymentOrchestrationService = paymentOrchestrationService;
        this.serviceProviderRepository = serviceProviderRepository;
        this.paymentCategoryRepository = paymentCategoryRepository;
    }

    /**
     * Pay credit card bill
     * Steps:
     * 1. Validate card issuer code and card number format (last 4 digits)
     * 2. Validate credit card issuer exists and is active
     * 3. Support partial and full payment amounts
     * 4. Build utility payment request
     * 5. Call payment orchestration service
     * 
     * @param request Credit card payment request
     * @return Payment response with transaction details
     * @throws PaymentProcessingException if validation fails or payment processing fails
     */
    public UtilityPaymentResponse payCreditCardBill(CreditCardPaymentRequest request) {
        log.info("Processing credit card bill payment for issuer: {}, card: **{}, amount: {}", 
                request.getIssuerCode(), 
                request.getCardLast4Digits(), 
                request.getAmount());

        // Step 1: Validate card issuer code and card number format
        validateCardDetails(request.getIssuerCode(), request.getCardLast4Digits());

        // Step 2: Validate credit card issuer exists and is active
        ServiceProvider issuer = validateCreditCardIssuer(request.getIssuerCode());

        // Step 3: Build utility payment request (supports partial and full payment)
        UtilityPaymentRequest paymentRequest = buildPaymentRequest(request, issuer);

        // Step 4: Call payment orchestration service
        log.info("Initiating payment orchestration for credit card bill");
        UtilityPaymentResponse response = paymentOrchestrationService.processUtilityPayment(paymentRequest);

        log.info("Credit card bill payment completed successfully. Transaction Ref: {}", 
                response.getTransactionRef());
        
        // Note: Payment confirmation to issuer is handled by the provider gateway
        return response;
    }

    /**
     * Get all active credit card issuers
     * Fetches all service providers in the credit card bill category that are active
     * 
     * @return List of credit card issuers as PaymentCategoryResponse
     */
    public List<PaymentCategoryResponse> getCreditCardIssuers() {
        log.info("Fetching all active credit card issuers");

        PaymentCategory creditCardCategory = paymentCategoryRepository
                .findByName(CREDIT_CARD_BILL_CATEGORY)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Credit card bill category not found"));

        List<ServiceProvider> issuers = serviceProviderRepository
                .findByCategoryAndIsActiveTrue(creditCardCategory);

        log.info("Found {} active credit card issuers", issuers.size());

        return issuers.stream()
                .map(this::mapToIssuerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate card details including issuer code and card number format
     */
    private void validateCardDetails(String issuerCode, String cardLast4Digits) {
        // Validate issuer code
        if (issuerCode == null || issuerCode.trim().isEmpty()) {
            throw new PaymentProcessingException("Credit card issuer code is required");
        }

        // Validate card last 4 digits
        if (cardLast4Digits == null || cardLast4Digits.trim().isEmpty()) {
            throw new PaymentProcessingException("Card last 4 digits are required");
        }

        if (!cardLast4Digits.matches("^[0-9]{4}$")) {
            throw new PaymentProcessingException(
                    "Invalid card number format. Card last 4 digits must be exactly 4 digits");
        }

        log.debug("Card details validation passed: issuer={}, card=**{}", 
                issuerCode, cardLast4Digits);
    }

    /**
     * Validate credit card issuer exists and is active
     */
    private ServiceProvider validateCreditCardIssuer(String issuerCode) {
        ServiceProvider issuer = serviceProviderRepository
                .findByProviderCode(issuerCode)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid credit card issuer code: " + issuerCode));

        if (!issuer.getIsActive()) {
            throw new PaymentProcessingException(
                    "Credit card issuer is currently unavailable: " + issuer.getProviderName());
        }

        // Verify issuer belongs to credit card bill category
        if (!CREDIT_CARD_BILL_CATEGORY.equalsIgnoreCase(issuer.getCategory().getName())) {
            throw new PaymentProcessingException(
                    "Provider " + issuer.getProviderName() + " is not a credit card issuer");
        }

        log.debug("Credit card issuer validation passed: {} ({})", 
                issuer.getProviderName(), issuerCode);
        return issuer;
    }

    /**
     * Build utility payment request from credit card payment request
     * Supports both partial and full payment amounts as specified by the user
     */
    private UtilityPaymentRequest buildPaymentRequest(
            CreditCardPaymentRequest request, 
            ServiceProvider issuer) {
        
        String description = String.format(
                "Credit card bill payment for card ending **%s via %s (Amount: %.2f)",
                request.getCardLast4Digits(),
                issuer.getProviderName(),
                request.getAmount()
        );

        // Use card last 4 digits as account identifier
        String accountIdentifier = "CC-" + request.getCardLast4Digits();

        return UtilityPaymentRequest.builder()
                .upiId(request.getUpiId())
                .providerCode(request.getIssuerCode())
                .categoryName(CREDIT_CARD_BILL_CATEGORY)
                .accountIdentifier(accountIdentifier)
                .amount(request.getAmount())
                .description(description)
                .build();
    }

    /**
     * Map ServiceProvider to PaymentCategoryResponse (issuer response)
     */
    private PaymentCategoryResponse mapToIssuerResponse(ServiceProvider provider) {
        return PaymentCategoryResponse.builder()
                .id(provider.getId())
                .name(provider.getProviderCode())
                .displayName(provider.getProviderName())
                .iconUrl(null) // Can be added later if needed
                .isActive(provider.getIsActive())
                .build();
    }
}
