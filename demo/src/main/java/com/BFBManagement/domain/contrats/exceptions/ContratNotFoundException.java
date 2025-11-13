package com.BFBManagement.domain.contrats.exceptions;

/**
 * Exception levée lorsqu'un contrat n'est pas trouvé.
 */
public class ContratNotFoundException extends RuntimeException {
    public ContratNotFoundException(String message) {
        super(message);
    }
}
