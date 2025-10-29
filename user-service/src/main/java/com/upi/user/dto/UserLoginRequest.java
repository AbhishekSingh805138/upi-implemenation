package com.upi.user.dto;

import jakarta.validation.constraints.NotBlank;

public class UserLoginRequest {
    
    @NotBlank(message = "Username or email is required")
    private String identifier; // Can be username or email
    
    // Constructors
    public UserLoginRequest() {}
    
    public UserLoginRequest(String identifier) {
        this.identifier = identifier;
    }
    
    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    @Override
    public String toString() {
        return "UserLoginRequest{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}