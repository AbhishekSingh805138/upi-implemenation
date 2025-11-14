package com.upi.utility.exception;

/**
 * Exception thrown when a duplicate transaction is detected
 * Used for idempotency violations
 */
public class DuplicateTransactionException extends RuntimeException {
    
    public DuplicateTransactionException(String message) {
        super(message);
    }
    
    public DuplicateTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
