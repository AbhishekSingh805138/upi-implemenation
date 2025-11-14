package com.upi.utility.service;

import com.upi.utility.dto.SavedBillerRequest;
import com.upi.utility.dto.SavedBillerResponse;
import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.SavedBiller;
import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.SavedBillerRepository;
import com.upi.utility.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing saved billers
 * Allows users to save frequently used billers for quick payments
 */
@Service
@Slf4j
public class SavedBillerService {

    private final SavedBillerRepository savedBillerRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public SavedBillerService(
            SavedBillerRepository savedBillerRepository,
            PaymentCategoryRepository paymentCategoryRepository,
            ServiceProviderRepository serviceProviderRepository) {
        this.savedBillerRepository = savedBillerRepository;
        this.paymentCategoryRepository = paymentCategoryRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    /**
     * Save a new biller
     * Validates biller details and checks for duplicates before saving
     * 
     * @param request Saved biller request with user ID, category, provider, and account details
     * @return Saved biller response with ID and details
     * @throws PaymentProcessingException if validation fails or duplicate exists
     */
    @Transactional
    public SavedBillerResponse saveBiller(SavedBillerRequest request) {
        log.info("Saving biller for user: {}, category: {}, provider: {}", 
                request.getUserId(), request.getCategoryName(), request.getProviderCode());

        // Validate biller details
        PaymentCategory category = validateCategory(request.getCategoryName());
        ServiceProvider provider = validateProvider(request.getProviderCode(), category);

        // Check for duplicate biller
        checkForDuplicate(request.getUserId(), category, provider.getId(), request.getAccountIdentifier());

        // Create and save biller
        SavedBiller biller = new SavedBiller();
        biller.setUserId(request.getUserId());
        biller.setCategory(category);
        biller.setProvider(provider);
        biller.setAccountIdentifier(request.getAccountIdentifier());
        biller.setNickname(request.getNickname());
        biller.setAccountHolderName(request.getAccountHolderName());

        SavedBiller savedBiller = savedBillerRepository.save(biller);
        
        log.info("Biller saved successfully with ID: {}", savedBiller.getId());
        return mapToResponse(savedBiller);
    }

    /**
     * Get all saved billers for a user
     * 
     * @param userId The user ID
     * @return List of saved billers
     */
    public List<SavedBillerResponse> getSavedBillers(Long userId) {
        log.info("Fetching saved billers for user: {}", userId);

        List<SavedBiller> billers = savedBillerRepository.findByUserId(userId);
        
        log.info("Found {} saved billers for user: {}", billers.size(), userId);
        return billers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get saved billers by category for a user
     * 
     * @param userId The user ID
     * @param categoryName The category name
     * @return List of saved billers for the category
     */
    public List<SavedBillerResponse> getBillersByCategory(Long userId, String categoryName) {
        log.info("Fetching saved billers for user: {}, category: {}", userId, categoryName);

        PaymentCategory category = validateCategory(categoryName);
        List<SavedBiller> billers = savedBillerRepository.findByUserIdAndCategory(userId, category);
        
        log.info("Found {} saved billers for user: {}, category: {}", 
                billers.size(), userId, categoryName);
        return billers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a saved biller
     * 
     * @param id The biller ID
     * @param request Updated biller details
     * @return Updated biller response
     * @throws PaymentProcessingException if biller not found
     */
    @Transactional
    public SavedBillerResponse updateBiller(Long id, SavedBillerRequest request) {
        log.info("Updating biller with ID: {}", id);

        SavedBiller biller = savedBillerRepository.findById(id)
                .orElseThrow(() -> new PaymentProcessingException("Saved biller not found with ID: " + id));

        // Update editable fields
        if (request.getNickname() != null) {
            biller.setNickname(request.getNickname());
        }
        if (request.getAccountHolderName() != null) {
            biller.setAccountHolderName(request.getAccountHolderName());
        }

        SavedBiller updatedBiller = savedBillerRepository.save(biller);
        
        log.info("Biller updated successfully with ID: {}", updatedBiller.getId());
        return mapToResponse(updatedBiller);
    }

    /**
     * Delete a saved biller
     * 
     * @param id The biller ID
     * @throws PaymentProcessingException if biller not found
     */
    @Transactional
    public void deleteBiller(Long id) {
        log.info("Deleting biller with ID: {}", id);

        if (!savedBillerRepository.existsById(id)) {
            throw new PaymentProcessingException("Saved biller not found with ID: " + id);
        }

        savedBillerRepository.deleteById(id);
        log.info("Biller deleted successfully with ID: {}", id);
    }

    /**
     * Validate payment category
     */
    private PaymentCategory validateCategory(String categoryName) {
        return paymentCategoryRepository.findByName(categoryName)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid category: " + categoryName));
    }

    /**
     * Validate service provider and ensure it belongs to the category
     */
    private ServiceProvider validateProvider(String providerCode, PaymentCategory category) {
        ServiceProvider provider = serviceProviderRepository.findByProviderCode(providerCode)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid provider code: " + providerCode));

        if (!provider.getCategory().getId().equals(category.getId())) {
            throw new PaymentProcessingException(
                    "Provider " + provider.getProviderName() + 
                    " does not belong to category " + category.getName());
        }

        if (!provider.getIsActive()) {
            throw new PaymentProcessingException(
                    "Provider is currently unavailable: " + provider.getProviderName());
        }

        return provider;
    }

    /**
     * Check for duplicate biller using unique constraint
     */
    private void checkForDuplicate(Long userId, PaymentCategory category, Long providerId, String accountIdentifier) {
        boolean exists = savedBillerRepository.existsByUserIdAndCategoryAndProviderIdAndAccountIdentifier(
                userId, category, providerId, accountIdentifier);

        if (exists) {
            throw new PaymentProcessingException(
                    "Biller already exists for this account. Please use a different account or update the existing biller.");
        }
    }

    /**
     * Map SavedBiller entity to SavedBillerResponse DTO
     */
    private SavedBillerResponse mapToResponse(SavedBiller biller) {
        return SavedBillerResponse.builder()
                .id(biller.getId())
                .userId(biller.getUserId())
                .categoryName(biller.getCategory().getName())
                .categoryDisplayName(biller.getCategory().getDisplayName())
                .providerCode(biller.getProvider().getProviderCode())
                .providerName(biller.getProvider().getProviderName())
                .accountIdentifier(biller.getAccountIdentifier())
                .nickname(biller.getNickname())
                .accountHolderName(biller.getAccountHolderName())
                .createdAt(biller.getCreatedAt())
                .updatedAt(biller.getUpdatedAt())
                .build();
    }
}
