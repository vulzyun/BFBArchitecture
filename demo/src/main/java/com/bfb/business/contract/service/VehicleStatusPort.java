package com.bfb.business.contract.service;

import com.bfb.business.vehicle.model.VehicleStatus;

import java.util.UUID;

/**
 * Port interface for external vehicle status service.
 * Defined in business layer, implemented in infrastructure layer.
 */
public interface VehicleStatusPort {
    VehicleStatus getStatus(UUID vehicleId);
}
