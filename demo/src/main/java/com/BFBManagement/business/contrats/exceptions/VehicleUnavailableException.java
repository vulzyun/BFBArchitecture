package com.BFBManagement.business.contrats.exceptions;

/**
 * Exception levée lorsqu'un véhicule est indisponible (en panne, etc.).
 */
public class VehicleUnavailableException extends RuntimeException {
    public VehicleUnavailableException(String message) {
        super(message);
    }
}
