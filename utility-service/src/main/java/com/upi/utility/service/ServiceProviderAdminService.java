package com.upi.utility.service;

import com.upi.utility.dto.ServiceProviderRequest;
import com.upi.utility.dto.ServiceProviderResponse;
import com.upi.utility.entity.PaymentCategory;
import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.exception.PaymentProcessingException;
import com.upi.utility.gateway.ServiceProviderGateway;
import com.upi.utility.gateway.ServiceProviderGatewayFactory;
import com.upi.utility.repository.PaymentCategoryRepository;
import com.upi.utility.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin service for managing service providers
 * Provides functionality to add, update, enable/disable service providers
 */
@Service
@Slf4j
public class ServiceProviderAdminService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final PaymentCategoryRepository paymentCategoryRepository;
    private final ServiceProviderGatewayFactory gatewayFactory;

    public ServiceProviderAdminService(
            ServiceProviderRepository serviceProviderRepository,
            PaymentCategoryRepository paymentCategoryRepository,
            ServiceProviderGatewayFactory gatewayFactory) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.paymentCategoryRepository = paymentCategoryRepository;
        this.gatewayFactory = gatewayFactory;
    }

    /**
     * Add a new service provider
     * Steps:
     * 1. Validate provider details (category, name, code, API endpoint)
     * 2. Check if provider code already exists
     * 3. Encrypt API key before storing
     * 4. Save provider to database
     * 
     * @param request Service provider request with all provider details
     * @return Created service provider response
     * @throws PaymentProcessingException if validation fails or provider already exists
     */
    @Transactional
    public ServiceProviderResponse addServiceProvider(ServiceProviderRequest request) {
        log.info("Adding new service provider: {}, category: {}", 
                request.getProviderName(), request.getCategoryName());

        // Step 1: Validate provider details
        validateProviderRequest(request);

        // Step 2: Check if provider code already exists
        if (serviceProviderRepository.findByProviderCode(request.getProviderCode()).isPresent()) {
            throw new PaymentProcessingException(
                    "Provider with code " + request.getProviderCode() + " already exists");
        }

        // Get payment category
        PaymentCategory category = paymentCategoryRepository
                .findByName(request.getCategoryName())
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid category: " + request.getCategoryName()));

        // Step 3: Encrypt API key before storing
        String encryptedApiKey = encryptApiKey(request.getApiKey());

        // Step 4: Save provider to database
        ServiceProvider provider = new ServiceProvider();
        provider.setCategory(category);
        provider.setProviderName(request.getProviderName());
        provider.setProviderCode(request.getProviderCode());
        provider.setApiEndpoint(request.getApiEndpoint());
        provider.setApiKeyEncrypted(encryptedApiKey);
        provider.setIsActive(false); // New providers start as inactive

        ServiceProvider savedProvider = serviceProviderRepository.save(provider);
        
        log.info("Service provider added successfully: {} (ID: {})", 
                savedProvider.getProviderName(), savedProvider.getId());
        
        return mapToProviderResponse(savedProvider);
    }

    /**
     * Get all service providers
     * Returns all providers including active and inactive
     * 
     * @return List of all service providers
     */
    public List<ServiceProviderResponse> getAllProviders() {
        log.info("Fetching all service providers");

        List<ServiceProvider> providers = serviceProviderRepository.findAll();
        
        log.info("Found {} service providers", providers.size());
        return providers.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get providers by category
     * 
     * @param categoryName The category name
     * @return List of providers in the category
     */
    public List<ServiceProviderResponse> getProvidersByCategory(String categoryName) {
        log.info("Fetching service providers for category: {}", categoryName);

        PaymentCategory category = paymentCategoryRepository
                .findByName(categoryName)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Invalid category: " + categoryName));

        List<ServiceProvider> providers = serviceProviderRepository.findByCategory(category);
        
        log.info("Found {} service providers for category: {}", providers.size(), categoryName);
        return providers.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update service provider details
     * Steps:
     * 1. Validate provider exists
     * 2. Validate update request
     * 3. Update provider details
     * 4. Encrypt new API key if provided
     * 5. Save updated provider
     * 
     * @param id Provider ID
     * @param request Updated provider details
     * @return Updated service provider response
     * @throws PaymentProcessingException if provider not found or validation fails
     */
    @Transactional
    public ServiceProviderResponse updateProvider(Long id, ServiceProviderRequest request) {
        log.info("Updating service provider ID: {}", id);

        // Step 1: Validate provider exists
        ServiceProvider provider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Service provider not found with ID: " + id));

        // Step 2: Validate update request
        validateProviderRequest(request);

        // Step 3: Update provider details
        if (request.getCategoryName() != null) {
            PaymentCategory category = paymentCategoryRepository
                    .findByName(request.getCategoryName())
                    .orElseThrow(() -> new PaymentProcessingException(
                            "Invalid category: " + request.getCategoryName()));
            provider.setCategory(category);
        }

        if (request.getProviderName() != null) {
            provider.setProviderName(request.getProviderName());
        }

        if (request.getApiEndpoint() != null) {
            provider.setApiEndpoint(request.getApiEndpoint());
        }

        // Step 4: Encrypt new API key if provided
        if (request.getApiKey() != null && !request.getApiKey().trim().isEmpty()) {
            String encryptedApiKey = encryptApiKey(request.getApiKey());
            provider.setApiKeyEncrypted(encryptedApiKey);
        }

        // Step 5: Save updated provider
        ServiceProvider updatedProvider = serviceProviderRepository.save(provider);
        
        log.info("Service provider updated successfully: {} (ID: {})", 
                updatedProvider.getProviderName(), updatedProvider.getId());
        
        return mapToProviderResponse(updatedProvider);
    }

    /**
     * Toggle provider status (enable/disable)
     * Steps:
     * 1. Validate provider exists
     * 2. If activating, validate API connectivity
     * 3. Toggle provider status
     * 4. Save provider
     * 
     * @param id Provider ID
     * @param isActive New active status
     * @return Updated service provider response
     * @throws PaymentProcessingException if provider not found or validation fails
     */
    @Transactional
    public ServiceProviderResponse toggleProviderStatus(Long id, Boolean isActive) {
        log.info("Toggling provider status for ID: {}, new status: {}", id, isActive);

        // Step 1: Validate provider exists
        ServiceProvider provider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Service provider not found with ID: " + id));

        // Step 2: If activating, validate API connectivity
        if (isActive != null && isActive && !provider.getIsActive()) {
            log.info("Activating provider: {}. Validating API connectivity...", 
                    provider.getProviderName());
            
            boolean isConnected = validateProviderConnectivity(provider.getProviderCode());
            if (!isConnected) {
                throw new PaymentProcessingException(
                        "Cannot activate provider. API connectivity validation failed for: " + 
                        provider.getProviderName());
            }
            
            log.info("API connectivity validated successfully for provider: {}", 
                    provider.getProviderName());
        }

        // Step 3: Toggle provider status
        if (isActive != null) {
            provider.setIsActive(isActive);
        }

        // Step 4: Save provider
        ServiceProvider updatedProvider = serviceProviderRepository.save(provider);
        
        log.info("Provider status updated successfully: {} (ID: {}), active: {}", 
                updatedProvider.getProviderName(), 
                updatedProvider.getId(), 
                updatedProvider.getIsActive());
        
        return mapToProviderResponse(updatedProvider);
    }

    /**
     * Delete service provider
     * Note: This is a soft delete - provider is marked as inactive
     * 
     * @param id Provider ID
     * @throws PaymentProcessingException if provider not found
     */
    @Transactional
    public void deleteProvider(Long id) {
        log.info("Deleting service provider ID: {}", id);

        ServiceProvider provider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new PaymentProcessingException(
                        "Service provider not found with ID: " + id));

        // Soft delete - mark as inactive
        provider.setIsActive(false);
        serviceProviderRepository.save(provider);
        
        log.info("Service provider soft deleted: {} (ID: {})", 
                provider.getProviderName(), provider.getId());
    }

    /**
     * Validate provider request
     */
    private void validateProviderRequest(ServiceProviderRequest request) {
        if (request.getProviderName() == null || request.getProviderName().trim().isEmpty()) {
            throw new PaymentProcessingException("Provider name is required");
        }

        if (request.getProviderCode() == null || request.getProviderCode().trim().isEmpty()) {
            throw new PaymentProcessingException("Provider code is required");
        }

        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new PaymentProcessingException("Category name is required");
        }

        if (request.getApiEndpoint() == null || request.getApiEndpoint().trim().isEmpty()) {
            throw new PaymentProcessingException("API endpoint is required");
        }

        // Validate API endpoint format (basic URL validation)
        if (!request.getApiEndpoint().startsWith("http://") && 
            !request.getApiEndpoint().startsWith("https://")) {
            throw new PaymentProcessingException(
                    "Invalid API endpoint format. Must start with http:// or https://");
        }
    }

    /**
     * Encrypt API key before storing
     * In production, use proper encryption (AES, RSA, etc.)
     * For now, using Base64 encoding as placeholder
     */
    private String encryptApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }
        
        // TODO: Replace with proper encryption in production
        return Base64.getEncoder().encodeToString(apiKey.getBytes());
    }

    /**
     * Validate provider API connectivity
     * Checks if the provider's API is reachable and responding
     */
    private boolean validateProviderConnectivity(String providerCode) {
        try {
            ServiceProviderGateway gateway = gatewayFactory.getGateway(providerCode);
            return gateway.validateProvider(providerCode);
        } catch (Exception e) {
            log.error("Provider connectivity validation failed for: {}", providerCode, e);
            return false;
        }
    }

    /**
     * Map ServiceProvider entity to ServiceProviderResponse DTO
     */
    private ServiceProviderResponse mapToProviderResponse(ServiceProvider provider) {
        return ServiceProviderResponse.builder()
                .id(provider.getId())
                .categoryName(provider.getCategory().getName())
                .categoryDisplayName(provider.getCategory().getDisplayName())
                .providerName(provider.getProviderName())
                .providerCode(provider.getProviderCode())
                .apiEndpoint(provider.getApiEndpoint())
                .isActive(provider.getIsActive())
                .build();
    }
}
