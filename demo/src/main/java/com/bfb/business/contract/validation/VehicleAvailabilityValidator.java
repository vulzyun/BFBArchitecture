package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.VehicleUnavailableException;
import com.bfb.business.vehicle.service.VehicleService;
import com.bfb.business.vehicle.model.VehicleStatus;
import org.springframework.stereotype.Component;

/**
 * Validates that the vehicle is available for rental (not broken).
 * Uses VehicleService directly (3-tier architecture).
 */
@Component
public class VehicleAvailabilityValidator implements ContractValidator {

    private final VehicleService vehicleService;

    public VehicleAvailabilityValidator(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Override
    public void validate(ContractCreationContext context) {
        VehicleStatus vehicleStatus = vehicleService.getStatus(context.getVehicleId());
        if (vehicleStatus == VehicleStatus.BROKEN) {
            throw new VehicleUnavailableException(
                String.format("Vehicle '%s' is currently broken and cannot be rented. " +
                    "Please choose another vehicle or wait for repairs.", context.getVehicleId())
            );
        }
    }
}
