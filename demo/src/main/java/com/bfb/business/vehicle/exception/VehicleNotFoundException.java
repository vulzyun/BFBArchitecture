package com.bfb.business.vehicle.exception;

/**
 * Exception thrown when a vehicle is not found.
 */
public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String message) {
        super(message);
    }
}
