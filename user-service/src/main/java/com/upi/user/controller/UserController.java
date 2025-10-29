package com.upi.user.controller;

import com.upi.user.dto.*;
import com.upi.user.entity.User;
import com.upi.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        logger.info("Received user registration request for username: {}", request.getUsername());
        
        User user = userService.registerUser(request);
        UserResponse response = new UserResponse(user);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * User login (basic validation without password)
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
        logger.info("Received login request for identifier: {}", request.getIdentifier());
        
        User user = userService.loginUser(request.getIdentifier());
        UserResponse response = new UserResponse(user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.debug("Received request to get user by ID: {}", id);
        
        User user = userService.getUserById(id);
        UserResponse response = new UserResponse(user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        logger.debug("Received request to get user by username: {}", username);
        
        User user = userService.getUserByUsername(username);
        UserResponse response = new UserResponse(user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, 
                                                  @Valid @RequestBody UserUpdateRequest request) {
        logger.info("Received request to update user with ID: {}", id);
        
        User user = userService.updateUser(id, request);
        UserResponse response = new UserResponse(user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate if user exists by ID
     */
    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateUserExists(@PathVariable Long id) {
        logger.debug("Received request to validate user existence for ID: {}", id);
        
        boolean exists = userService.validateUserExists(id);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * Get all users (for admin purposes)
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.debug("Received request to get all users");
        
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Delete user by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Received request to delete user with ID: {}", id);
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("User Service is running");
    }
}