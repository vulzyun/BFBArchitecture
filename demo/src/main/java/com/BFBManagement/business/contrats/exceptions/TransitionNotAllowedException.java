package com.BFBManagement.business.contrats.exceptions;

/**
 * Exception levée lorsqu'une transition d'état n'est pas autorisée.
 */
public class TransitionNotAllowedException extends RuntimeException {
    public TransitionNotAllowedException(String message) {
        super(message);
    }
}
