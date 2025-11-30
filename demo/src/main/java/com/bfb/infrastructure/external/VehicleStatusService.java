package com.bfb.infrastructure.external;

import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.contract.service.VehicleStatusPort;
import com.bfb.business.vehicle.service.VehicleService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of VehicleStatusPort using VehicleService.
 * This adapter connects the contract domain to the vehicle domain.
 */
@Component
public class VehicleStatusService implements VehicleStatusPort {

    private final VehicleService vehicleService;

    public VehicleStatusService(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Override
    public VehicleStatus getStatus(UUID vehicleId) {
        return vehicleService.getStatus(vehicleId);
    }
}
