package com.upi.utility.controller;

import com.upi.utility.dto.*;
import com.upi.utility.service.CreditCardBillService;
import com.upi.utility.service.ElectricityBillService;
import com.upi.utility.service.InsurancePremiumService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for bill payment operations
 * Provides endpoints for electricity, credit card, and insurance bill payments
 */
@RestController
@RequestMapping("/api/utilities/bills")
@Slf4j
public class BillPaymentController {

    private final ElectricityBillService electricityBillService;
    private final CreditCardBillService creditCardBillService;
    private final InsurancePremiumService insurancePremiumService;

    public BillPaymentController(
            ElectricityBillService electricityBillService,
            CreditCardBillService creditCardBillService,
            InsurancePremiumService insurancePremiumService) {
        this.electricityBillService = electricityBillService;
        this.creditCardBillService = creditCardBillService;
        this.insurancePremiumService = insurancePremiumService;
    }

    // ========== Electricity Bill Endpoints ==========

    /**
     * POST /api/utilities/bills/electricity
     * Pay electricity bill
     * 
     * @param request Electricity bill payment request
     * @return Payment response with transaction details
     */
    @PostMapping("/electricity")
    public ResponseEntity<UtilityPaymentResponse> payElectricityBill(
            @Valid @RequestBody ElectricityBillPaymentRequest request) {
        log.info("POST /api/utilities/bills/electricity - Processing electricity bill payment");
        UtilityPaymentResponse response = electricityBillService.payElectricityBill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/utilities/bills/electricity/providers
     * Get all electricity providers
     * 
     * @return List of electricity providers
     */
    @GetMapping("/electricity/providers")
    public ResponseEntity<List<PaymentCategoryResponse>> getElectricityProviders() {
        log.info("GET /api/utilities/bills/electricity/providers - Fetching electricity providers");
        List<PaymentCategoryResponse> providers = electricityBillService.getElectricityProviders();
        return ResponseEntity.ok(providers);
    }

    /**
     * GET /api/utilities/bills/electricity/fetch
     * Fetch electricity bill details
     * 
     * @param providerCode The provider code
     * @param consumerNumber The consumer number
     * @return Bill details
     */
    @GetMapping("/electricity/fetch")
    public ResponseEntity<BillDetails> fetchElectricityBill(
            @RequestParam String providerCode,
            @RequestParam String consumerNumber) {
        log.info("GET /api/utilities/bills/electricity/fetch - Fetching electricity bill");
        BillDetails billDetails = electricityBillService.fetchBillDetails(providerCode, consumerNumber);
        return ResponseEntity.ok(billDetails);
    }

    // ========== Credit Card Bill Endpoints ==========

    /**
     * POST /api/utilities/bills/credit-card
     * Pay credit card bill
     * 
     * @param request Credit card payment request
     * @return Payment response with transaction details
     */
    @PostMapping("/credit-card")
    public ResponseEntity<UtilityPaymentResponse> payCreditCardBill(
            @Valid @RequestBody CreditCardPaymentRequest request) {
        log.info("POST /api/utilities/bills/credit-card - Processing credit card bill payment");
        UtilityPaymentResponse response = creditCardBillService.payCreditCardBill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/utilities/bills/credit-card/issuers
     * Get all credit card issuers
     * 
     * @return List of credit card issuers
     */
    @GetMapping("/credit-card/issuers")
    public ResponseEntity<List<PaymentCategoryResponse>> getCreditCardIssuers() {
        log.info("GET /api/utilities/bills/credit-card/issuers - Fetching credit card issuers");
        List<PaymentCategoryResponse> issuers = creditCardBillService.getCreditCardIssuers();
        return ResponseEntity.ok(issuers);
    }

    // ========== Insurance Premium Endpoints ==========

    /**
     * POST /api/utilities/bills/insurance
     * Pay insurance premium
     * 
     * @param request Insurance premium request
     * @return Payment response with transaction details
     */
    @PostMapping("/insurance")
    public ResponseEntity<UtilityPaymentResponse> payInsurancePremium(
            @Valid @RequestBody InsurancePremiumRequest request) {
        log.info("POST /api/utilities/bills/insurance - Processing insurance premium payment");
        UtilityPaymentResponse response = insurancePremiumService.payInsurancePremium(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
