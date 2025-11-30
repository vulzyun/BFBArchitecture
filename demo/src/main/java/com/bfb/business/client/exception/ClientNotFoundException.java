package com.bfb.business.client.exception;

/**
 * Exception thrown when a client is not found.
 */
public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String message) {
        super(message);
    }
}
