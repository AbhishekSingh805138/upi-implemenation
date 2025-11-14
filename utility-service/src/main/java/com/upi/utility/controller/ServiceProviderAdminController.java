package com.upi.utility.controller;

import com.upi.utility.dto.ServiceProviderRequest;
import com.upi.utility.dto.ServiceProviderResponse;
import com.upi.utility.service.ServiceProviderAdminService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for service provider admin operations
 * Provides endpoints for managing service providers (admin only)
 */
@RestController
@RequestMapping("/api/utilities/admin/providers")
@Slf4j
public class ServiceProviderAdminController {

    private final ServiceProviderAdminService serviceProviderAdminService;

    public ServiceProviderAdminController(ServiceProviderAdminService serviceProviderAdminService) {
        this.serviceProviderAdminService = serviceProviderAdminService;
    }

    /**
     * POST /api/utilities/admin/providers
     * Add a new service provider
     * 
     * @param request Service provider request
     * @return Created service provider
     */
    @PostMapping
    public ResponseEntity<ServiceProviderResponse> addServiceProvider(
            @Valid @RequestBody ServiceProviderRequest request) {
        log.info("POST /api/utilities/admin/providers - Adding new service provider");
        ServiceProviderResponse response = serviceProviderAdminService.addServiceProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/utilities/admin/providers
     * Get all service providers
     * 
     * @return List of all service providers
     */
    @GetMapping
    public ResponseEntity<List<ServiceProviderResponse>> getAllProviders() {
        log.info("GET /api/utilities/admin/providers - Fetching all service providers");
        List<ServiceProviderResponse> providers = serviceProviderAdminService.getAllProviders();
        return ResponseEntity.ok(providers);
    }

    /**
     * GET /api/utilities/admin/providers/category/{categoryName}
     * Get providers by category
     * 
     * @param categoryName The category name
     * @return List of providers in the category
     */
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<ServiceProviderResponse>> getProvidersByCategory(
            @PathVariable String categoryName) {
        log.info("GET /api/utilities/admin/providers/category/{} - Fetching providers by category", 
                categoryName);
        List<ServiceProviderResponse> providers = serviceProviderAdminService
                .getProvidersByCategory(categoryName);
        return ResponseEntity.ok(providers);
    }

    /**
     * PUT /api/utilities/admin/providers/{id}
     * Update a service provider
     * 
     * @param id The provider ID
     * @param request Updated provider details
     * @return Updated service provider
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceProviderResponse> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ServiceProviderRequest request) {
        log.info("PUT /api/utilities/admin/providers/{} - Updating service provider", id);
        ServiceProviderResponse response = serviceProviderAdminService.updateProvider(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/utilities/admin/providers/{id}/status
     * Enable or disable a service provider
     * 
     * @param id The provider ID
     * @param isActive New active status
     * @return Updated service provider
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ServiceProviderResponse> toggleProviderStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        log.info("PUT /api/utilities/admin/providers/{}/status - Toggling provider status to: {}", 
                id, isActive);
        ServiceProviderResponse response = serviceProviderAdminService
                .toggleProviderStatus(id, isActive);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/utilities/admin/providers/{id}
     * Delete a service provider (soft delete)
     * 
     * @param id The provider ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        log.info("DELETE /api/utilities/admin/providers/{} - Deleting service provider", id);
        serviceProviderAdminService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }
}
