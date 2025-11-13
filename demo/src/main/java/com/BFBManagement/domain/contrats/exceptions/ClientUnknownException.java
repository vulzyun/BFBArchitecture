package com.BFBManagement.domain.contrats.exceptions;

/**
 * Exception levée lorsqu'un client est inconnu.
 */
public class ClientUnknownException extends RuntimeException {
    public ClientUnknownException(String message) {
        super(message);
    }
}
