package com.BFBManagement.domain.contrats.exceptions;

/**
 * Exception levée lorsqu'il y a un chevauchement de contrats sur un même véhicule.
 */
public class OverlapException extends RuntimeException {
    public OverlapException(String message) {
        super(message);
    }
}
