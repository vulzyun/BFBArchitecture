package com.bfb.business.client.exception;

/**
 * Exception thrown when trying to create a client with a duplicate email.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
