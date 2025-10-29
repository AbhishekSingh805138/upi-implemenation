package com.upi.account.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class UserServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;
    
    public UserServiceClient(RestTemplate restTemplate,
                            @Value("${user-service.base-url:http://user-service}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
        
        logger.info("UserServiceClient initialized with baseUrl: {}", userServiceBaseUrl);
    }
    
    /**
     * Validate if user exists by ID
     */
    public boolean validateUserExists(Long userId) {
        try {
            logger.debug("Validating user existence for ID: {}", userId);
            
            String url = userServiceBaseUrl + "/api/users/" + userId + "/validate";
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            
            logger.debug("User validation result for ID {}: {}", userId, exists);
            return exists != null && exists;
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.debug("User not found for ID: {}", userId);
                return false;
            }
            logger.error("Error validating user existence for ID {}: {}", userId, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error validating user existence for ID {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user details by ID
     */
    public UserDetails getUserById(Long userId) {
        try {
            logger.debug("Fetching user details for ID: {}", userId);
            
            String url = userServiceBaseUrl + "/api/users/" + userId;
            UserDetails user = restTemplate.getForObject(url, UserDetails.class);
            
            logger.debug("Successfully fetched user details for ID: {}", userId);
            return user;
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.debug("User not found for ID: {}", userId);
                return null;
            }
            logger.error("Error fetching user details for ID {}: {}", userId, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error fetching user details for ID {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * DTO for User details
     */
    public static class UserDetails {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String fullName;
        
        // Constructors
        public UserDetails() {}
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        @Override
        public String toString() {
            return "UserDetails{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", fullName='" + fullName + '\'' +
                    '}';
        }
    }
}