package com.upi.utility.controller;

import com.upi.utility.dto.PaymentCategoryResponse;
import com.upi.utility.service.PaymentCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for payment category operations
 * Provides endpoints to fetch available payment categories
 */
@RestController
@RequestMapping("/api/utilities/categories")
@Slf4j
public class PaymentCategoryController {

    private final PaymentCategoryService paymentCategoryService;

    public PaymentCategoryController(PaymentCategoryService paymentCategoryService) {
        this.paymentCategoryService = paymentCategoryService;
    }

    /**
     * GET /api/utilities/categories
     * Get all active payment categories
     * 
     * @return List of payment categories
     */
    @GetMapping
    public ResponseEntity<List<PaymentCategoryResponse>> getPaymentCategories() {
        log.info("GET /api/utilities/categories - Fetching payment categories");
        List<PaymentCategoryResponse> categories = paymentCategoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
}
