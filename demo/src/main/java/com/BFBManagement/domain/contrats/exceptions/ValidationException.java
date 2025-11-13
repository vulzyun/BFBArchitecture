package com.BFBManagement.domain.contrats.exceptions;

/**
 * Exception levée lors d'une validation métier échouée.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
