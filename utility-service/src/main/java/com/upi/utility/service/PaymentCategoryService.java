package com.upi.utility.service;

import com.upi.utility.dto.PaymentCategoryResponse;
import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.repository.PaymentCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing payment categories
 * Provides functionality to view available payment categories
 */
@Service
@Slf4j
public class PaymentCategoryService {

    private final PaymentCategoryRepository paymentCategoryRepository;

    public PaymentCategoryService(PaymentCategoryRepository paymentCategoryRepository) {
        this.paymentCategoryRepository = paymentCategoryRepository;
    }

    /**
     * Get all payment categories
     * Displays category name, display name, and icon for each category
     * Filters to show only active categories to users
     * Marks unavailable categories with appropriate message
     * 
     * @return List of all payment categories
     */
    public List<PaymentCategoryResponse> getAllCategories() {
        log.info("Fetching all payment categories");

        List<PaymentCategory> categories = paymentCategoryRepository.findAll();
        
        log.info("Found {} payment categories", categories.size());
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get only active payment categories
     * 
     * @return List of active payment categories
     */
    public List<PaymentCategoryResponse> getActiveCategories() {
        log.info("Fetching active payment categories");

        List<PaymentCategory> categories = paymentCategoryRepository.findByIsActiveTrue();
        
        log.info("Found {} active payment categories", categories.size());
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map PaymentCategory entity to PaymentCategoryResponse DTO
     */
    private PaymentCategoryResponse mapToCategoryResponse(PaymentCategory category) {
        return PaymentCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .displayName(category.getDisplayName())
                .iconUrl(category.getIconUrl())
                .isActive(category.getIsActive())
                .build();
    }
}
