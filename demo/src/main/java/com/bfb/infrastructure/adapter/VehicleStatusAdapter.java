package com.bfb.infrastructure.adapter;

import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.contract.service.VehicleStatusPort;
import com.bfb.business.vehicle.service.VehicleService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter implementing VehicleStatusPort using VehicleService.
 * This adapter connects the contract domain to the vehicle domain.
 */
@Component
public class VehicleStatusAdapter implements VehicleStatusPort {

    private final VehicleService vehicleService;

    public VehicleStatusAdapter(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @Override
    public VehicleStatus getStatus(UUID vehicleId) {
        return vehicleService.getStatus(vehicleId);
    }
}
