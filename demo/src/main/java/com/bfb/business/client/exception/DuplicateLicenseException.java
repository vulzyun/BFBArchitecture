package com.bfb.business.client.exception;

public class DuplicateLicenseException extends RuntimeException {
    public DuplicateLicenseException(String message) {
        super(message);
    }
}
