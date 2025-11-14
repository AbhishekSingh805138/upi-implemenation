package com.upi.utility.validation;

import com.upi.utility.entity.ServiceProvider;
import com.upi.utility.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Custom validator for provider codes
 * Ensures provider exists and is active
 */
@Component
@Slf4j
public class ProviderCodeValidator {

    private final ServiceProviderRepository serviceProviderRepository;

    public ProviderCodeValidator(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    /**
     * Check if provider code exists
     * 
     * @param providerCode The provider code to check
     * @return true if provider exists, false otherwise
     */
    public boolean exists(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            log.debug("Provider code is null or empty");
            return false;
        }

        Optional<ServiceProvider> provider = serviceProviderRepository.findByProviderCode(providerCode);
        boolean exists = provider.isPresent();
        log.debug("Provider code existence check for {}: {}", providerCode, exists);
        return exists;
    }

    /**
     * Check if provider is active
     * 
     * @param providerCode The provider code to check
     * @return true if provider exists and is active, false otherwise
     */
    public boolean isActive(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            log.debug("Provider code is null or empty");
            return false;
        }

        Optional<ServiceProvider> provider = serviceProviderRepository.findByProviderCode(providerCode);
        
        if (provider.isEmpty()) {
            log.debug("Provider not found: {}", providerCode);
            return false;
        }

        boolean isActive = provider.get().getIsActive();
        log.debug("Provider active check for {}: {}", providerCode, isActive);
        return isActive;
    }

    /**
     * Validate provider code (exists and is active)
     * 
     * @param providerCode The provider code to validate
     * @return true if provider exists and is active, false otherwise
     */
    public boolean isValid(String providerCode) {
        return exists(providerCode) && isActive(providerCode);
    }

    /**
     * Check if provider belongs to a specific category
     * 
     * @param providerCode The provider code to check
     * @param categoryName The expected category name
     * @return true if provider exists and belongs to the category, false otherwise
     */
    public boolean belongsToCategory(String providerCode, String categoryName) {
        if (providerCode == null || categoryName == null) {
            log.debug("Provider code or category name is null");
            return false;
        }

        Optional<ServiceProvider> provider = serviceProviderRepository.findByProviderCode(providerCode);
        
        if (provider.isEmpty()) {
            log.debug("Provider not found: {}", providerCode);
            return false;
        }

        boolean belongsToCategory = provider.get().getCategory().getName()
                .equalsIgnoreCase(categoryName);
        log.debug("Provider {} belongs to category {}: {}", 
                providerCode, categoryName, belongsToCategory);
        return belongsToCategory;
    }

    /**
     * Get provider name by code
     * 
     * @param providerCode The provider code
     * @return Provider name if found, null otherwise
     */
    public String getProviderName(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            return null;
        }

        return serviceProviderRepository.findByProviderCode(providerCode)
                .map(ServiceProvider::getProviderName)
                .orElse(null);
    }
}
