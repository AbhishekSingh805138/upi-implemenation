package com.upi.utility.controller;

import com.upi.utility.dto.MobileRechargeRequest;
import com.upi.utility.dto.PaymentCategoryResponse;
import com.upi.utility.dto.RechargePlanResponse;
import com.upi.utility.dto.UtilityPaymentResponse;
import com.upi.utility.service.MobileRechargeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for mobile recharge operations
 * Provides endpoints for mobile recharge, operators, and plans
 */
@RestController
@RequestMapping("/api/utilities/recharge/mobile")
@Slf4j
public class MobileRechargeController {

    private final MobileRechargeService mobileRechargeService;

    public MobileRechargeController(MobileRechargeService mobileRechargeService) {
        this.mobileRechargeService = mobileRechargeService;
    }

    /**
     * POST /api/utilities/recharge/mobile
     * Process mobile recharge
     * 
     * @param request Mobile recharge request
     * @return Payment response with transaction details
     */
    @PostMapping
    public ResponseEntity<UtilityPaymentResponse> processMobileRecharge(
            @Valid @RequestBody MobileRechargeRequest request) {
        log.info("POST /api/utilities/recharge/mobile - Processing mobile recharge");
        UtilityPaymentResponse response = mobileRechargeService.processMobileRecharge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/utilities/recharge/mobile/operators
     * Get all mobile operators
     * 
     * @return List of mobile operators
     */
    @GetMapping("/operators")
    public ResponseEntity<List<PaymentCategoryResponse>> getMobileOperators() {
        log.info("GET /api/utilities/recharge/mobile/operators - Fetching mobile operators");
        List<PaymentCategoryResponse> operators = mobileRechargeService.getMobileOperators();
        return ResponseEntity.ok(operators);
    }

    /**
     * GET /api/utilities/recharge/mobile/plans/{operator}
     * Get recharge plans for an operator
     * 
     * @param operator The operator code
     * @return List of recharge plans
     */
    @GetMapping("/plans/{operator}")
    public ResponseEntity<List<RechargePlanResponse>> getRechargePlans(
            @PathVariable String operator) {
        log.info("GET /api/utilities/recharge/mobile/plans/{} - Fetching recharge plans", operator);
        List<RechargePlanResponse> plans = mobileRechargeService.getRechargePlans(operator);
        return ResponseEntity.ok(plans);
    }
}
