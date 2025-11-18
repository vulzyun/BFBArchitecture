package com.bfb.infrastructure.external;

import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.contract.service.VehicleStatusPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of VehicleStatusPort for external vehicle status service.
 */
@Component
public class VehicleStatusService implements VehicleStatusPort {

    @Override
    public VehicleStatus getStatus(UUID vehicleId) {
        // Placeholder: simulate external service call
        // In production, this would call an actual vehicle service
        return VehicleStatus.AVAILABLE;
    }
}
