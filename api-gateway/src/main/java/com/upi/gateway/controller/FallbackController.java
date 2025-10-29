package com.upi.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/account-service")
    public ResponseEntity<Map<String, Object>> accountServiceFallback() {
        return createFallbackResponse("Account Service is currently unavailable");
    }
    
    @GetMapping("/transaction-service")
    public ResponseEntity<Map<String, Object>> transactionServiceFallback() {
        return createFallbackResponse("Transaction Service is currently unavailable");
    }
    
    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return createFallbackResponse("User Service is currently unavailable");
    }
    
    @GetMapping("/general")
    public ResponseEntity<Map<String, Object>> generalFallback() {
        return createFallbackResponse("Service is temporarily unavailable");
    }
    
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "SERVICE_UNAVAILABLE");
        response.put("message", message);
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("timestamp", LocalDateTime.now());
        response.put("suggestion", "Please try again later or contact support if the issue persists");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}