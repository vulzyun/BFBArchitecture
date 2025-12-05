package com.bfb.business.contract.constants;

import java.time.LocalDate;
import java.util.UUID;

public final class ValidationMessages {
    private ValidationMessages() {}
    
    public static final String START_DATE_MUST_BE_BEFORE_END_DATE = 
        "Start date must be before end date";
    
    public static String clientNotFound(UUID clientId) {
        return "Client with ID " + clientId + " not found";
    }
    
    public static String vehicleNotFound(UUID vehicleId) {
        return "Vehicle with ID " + vehicleId + " not found";
    }
    
    public static String vehicleNotAvailable(UUID vehicleId) {
        return "Vehicle with ID " + vehicleId + " is not available";
    }
    
    public static String clientNotAdult(int currentAge, int requiredAge) {
        return "Client age " + currentAge + " is below required minimum age " + requiredAge;
    }
    
    public static String contractOverlap(UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        return "Contract for vehicle " + vehicleId + " overlaps with existing contract in period " + 
               startDate + " to " + endDate;
    }
    
    public static String duplicateClient(String email) {
        return "Client with email " + email + " already exists";
    }
    
    public static String duplicateLicense(String licenseNumber) {
        return "Client with license number " + licenseNumber + " already exists";
    }
    
    public static String duplicateVehicle(String registrationPlate) {
        return "Vehicle with registration plate " + registrationPlate + " already exists";
    }
}
