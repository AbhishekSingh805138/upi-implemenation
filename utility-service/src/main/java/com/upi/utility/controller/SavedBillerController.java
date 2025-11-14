package com.upi.utility.controller;

import com.upi.utility.dto.SavedBillerRequest;
import com.upi.utility.dto.SavedBillerResponse;
import com.upi.utility.service.SavedBillerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for saved biller operations
 * Provides endpoints to manage saved billers
 */
@RestController
@RequestMapping("/api/utilities/billers")
@Slf4j
public class SavedBillerController {

    private final SavedBillerService savedBillerService;

    public SavedBillerController(SavedBillerService savedBillerService) {
        this.savedBillerService = savedBillerService;
    }

    /**
     * POST /api/utilities/billers
     * Save a new biller
     * 
     * @param request Saved biller request
     * @return Saved biller response
     */
    @PostMapping
    public ResponseEntity<SavedBillerResponse> saveBiller(
            @Valid @RequestBody SavedBillerRequest request) {
        log.info("POST /api/utilities/billers - Saving new biller");
        SavedBillerResponse response = savedBillerService.saveBiller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/utilities/billers/{userId}
     * Get user's saved billers
     * 
     * @param userId The user ID
     * @return List of saved billers
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<SavedBillerResponse>> getSavedBillers(
            @PathVariable Long userId) {
        log.info("GET /api/utilities/billers/{} - Fetching saved billers", userId);
        List<SavedBillerResponse> billers = savedBillerService.getSavedBillers(userId);
        return ResponseEntity.ok(billers);
    }

    /**
     * GET /api/utilities/billers/{userId}/{category}
     * Get billers by category
     * 
     * @param userId The user ID
     * @param category The category name
     * @return List of saved billers for the category
     */
    @GetMapping("/{userId}/{category}")
    public ResponseEntity<List<SavedBillerResponse>> getBillersByCategory(
            @PathVariable Long userId,
            @PathVariable String category) {
        log.info("GET /api/utilities/billers/{}/{} - Fetching billers by category", 
                userId, category);
        List<SavedBillerResponse> billers = savedBillerService.getBillersByCategory(userId, category);
        return ResponseEntity.ok(billers);
    }

    /**
     * PUT /api/utilities/billers/{id}
     * Update a saved biller
     * 
     * @param id The biller ID
     * @param request Updated biller details
     * @return Updated biller response
     */
    @PutMapping("/{id}")
    public ResponseEntity<SavedBillerResponse> updateBiller(
            @PathVariable Long id,
            @Valid @RequestBody SavedBillerRequest request) {
        log.info("PUT /api/utilities/billers/{} - Updating saved biller", id);
        SavedBillerResponse response = savedBillerService.updateBiller(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/utilities/billers/{id}
     * Delete a saved biller
     * 
     * @param id The biller ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBiller(@PathVariable Long id) {
        log.info("DELETE /api/utilities/billers/{} - Deleting saved biller", id);
        savedBillerService.deleteBiller(id);
        return ResponseEntity.noContent().build();
    }
}
