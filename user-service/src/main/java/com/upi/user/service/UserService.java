package com.upi.user.service;

import com.upi.user.dto.UserRegistrationRequest;
import com.upi.user.dto.UserUpdateRequest;
import com.upi.user.entity.User;
import com.upi.user.exception.UserAlreadyExistsException;
import com.upi.user.exception.UserNotFoundException;
import com.upi.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Register a new user
     */
    public User registerUser(UserRegistrationRequest request) {
        logger.info("Registering new user with username: {}", request.getUsername());
        
        // Check if user already exists
        if (userRepository.existsByUsernameOrEmailOrPhone(
                request.getUsername(), request.getEmail(), request.getPhone())) {
            
            String conflictField = getConflictField(request);
            throw new UserAlreadyExistsException("User already exists with " + conflictField);
        }
        
        // Create new user
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPhone(),
                request.getFullName()
        );
        
        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user with ID: {}", savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Login user (basic validation without password)
     */
    public User loginUser(String identifier) {
        logger.info("User login attempt with identifier: {}", identifier);
        
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(identifier);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with identifier: " + identifier);
        }
        
        User user = userOpt.get();
        logger.info("User login successful for user ID: {}", user.getId());
        
        return user;
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        logger.debug("Fetching user with ID: {}", id);
        
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        logger.debug("Fetching user with username: {}", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        logger.debug("Fetching user with email: {}", email);
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
    
    /**
     * Update user profile
     */
    public User updateUser(Long id, UserUpdateRequest request) {
        logger.info("Updating user with ID: {}", id);
        
        User user = getUserById(id);
        
        // Update fields if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            // Check if phone is already taken by another user
            Optional<User> existingUser = userRepository.findByPhone(request.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new UserAlreadyExistsException("Phone number already exists: " + request.getPhone());
            }
            user.setPhone(request.getPhone());
        }
        
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        
        User updatedUser = userRepository.save(user);
        logger.info("Successfully updated user with ID: {}", updatedUser.getId());
        
        return updatedUser;
    }
    
    /**
     * Validate if user exists by ID
     */
    @Transactional(readOnly = true)
    public boolean validateUserExists(Long id) {
        logger.debug("Validating user existence with ID: {}", id);
        return userRepository.existsById(id);
    }
    
    /**
     * Get all users (for admin purposes)
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }
    
    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        logger.info("Successfully deleted user with ID: {}", id);
    }
    
    /**
     * Helper method to identify which field conflicts during registration
     */
    private String getConflictField(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return "username: " + request.getUsername();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return "email: " + request.getEmail();
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            return "phone: " + request.getPhone();
        }
        return "provided details";
    }
}