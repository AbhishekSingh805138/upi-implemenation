package com.upi.utility.controller;

import com.upi.utility.dto.DTHRechargeRequest;
import com.upi.utility.dto.PaymentCategoryResponse;
import com.upi.utility.dto.RechargePlanResponse;
import com.upi.utility.dto.UtilityPaymentResponse;
import com.upi.utility.service.DTHRechargeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for DTH recharge operations
 * Provides endpoints for DTH recharge, operators, and plans
 */
@RestController
@RequestMapping("/api/utilities/recharge/dth")
@Slf4j
public class DTHRechargeController {

    private final DTHRechargeService dthRechargeService;

    public DTHRechargeController(DTHRechargeService dthRechargeService) {
        this.dthRechargeService = dthRechargeService;
    }

    /**
     * POST /api/utilities/recharge/dth
     * Process DTH recharge
     * 
     * @param request DTH recharge request
     * @return Payment response with transaction details
     */
    @PostMapping
    public ResponseEntity<UtilityPaymentResponse> processDTHRecharge(
            @Valid @RequestBody DTHRechargeRequest request) {
        log.info("POST /api/utilities/recharge/dth - Processing DTH recharge");
        UtilityPaymentResponse response = dthRechargeService.processDTHRecharge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/utilities/recharge/dth/operators
     * Get all DTH operators
     * 
     * @return List of DTH operators
     */
    @GetMapping("/operators")
    public ResponseEntity<List<PaymentCategoryResponse>> getDTHOperators() {
        log.info("GET /api/utilities/recharge/dth/operators - Fetching DTH operators");
        List<PaymentCategoryResponse> operators = dthRechargeService.getDTHOperators();
        return ResponseEntity.ok(operators);
    }

    /**
     * GET /api/utilities/recharge/dth/plans/{operator}/{subscriberId}
     * Get DTH plans for an operator and subscriber
     * 
     * @param operator The operator code
     * @param subscriberId The subscriber ID
     * @return List of DTH plans
     */
    @GetMapping("/plans/{operator}/{subscriberId}")
    public ResponseEntity<List<RechargePlanResponse>> getDTHPlans(
            @PathVariable String operator,
            @PathVariable String subscriberId) {
        log.info("GET /api/utilities/recharge/dth/plans/{}/{} - Fetching DTH plans", 
                operator, subscriberId);
        List<RechargePlanResponse> plans = dthRechargeService.getDTHPlans(operator, subscriberId);
        return ResponseEntity.ok(plans);
    }
}
