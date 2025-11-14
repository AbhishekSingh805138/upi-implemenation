package com.upi.utility.exception;

/**
 * Exception thrown when account identifier validation fails
 * Used for invalid mobile numbers, consumer numbers, policy numbers, etc.
 */
public class InvalidAccountIdentifierException extends RuntimeException {
    
    public InvalidAccountIdentifierException(String message) {
        super(message);
    }
    
    public InvalidAccountIdentifierException(String message, Throwable cause) {
        super(message, cause);
    }
}
