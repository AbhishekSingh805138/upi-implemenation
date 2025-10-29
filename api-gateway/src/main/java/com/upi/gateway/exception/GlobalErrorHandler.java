package com.upi.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        
        // Determine error details
        ErrorResponse errorResponse = createErrorResponse(ex);
        
        // Log the error
        logger.error("Gateway Error: {} - {}", errorResponse.getError(), errorResponse.getMessage(), ex);
        
        // Set response headers
        response.setStatusCode(HttpStatus.valueOf(errorResponse.getStatus()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // Create response body
        String responseBody;
        try {
            responseBody = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing error response", e);
            responseBody = "{\"error\":\"INTERNAL_SERVER_ERROR\",\"message\":\"An unexpected error occurred\"}";
        }
        
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    private ErrorResponse createErrorResponse(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return new ErrorResponse(
                    rse.getStatusCode().toString(),
                    rse.getReason() != null ? rse.getReason() : "Service error",
                    rse.getStatusCode().value()
            );
        }
        
        // Handle connection errors (service unavailable)
        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return new ErrorResponse(
                    "SERVICE_UNAVAILABLE",
                    "The requested service is currently unavailable. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE.value()
            );
        }
        
        // Handle timeout errors
        if (ex.getMessage() != null && ex.getMessage().contains("timeout")) {
            return new ErrorResponse(
                    "GATEWAY_TIMEOUT",
                    "The request timed out. Please try again.",
                    HttpStatus.GATEWAY_TIMEOUT.value()
            );
        }
        
        // Default error response
        return new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred in the gateway",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }
    
    public static class ErrorResponse {
        private String error;
        private String message;
        private int status;
        private LocalDateTime timestamp;
        
        public ErrorResponse(String error, String message, int status) {
            this.error = error;
            this.message = message;
            this.status = status;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters and setters
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getStatus() {
            return status;
        }
        
        public void setStatus(int status) {
            this.status = status;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}