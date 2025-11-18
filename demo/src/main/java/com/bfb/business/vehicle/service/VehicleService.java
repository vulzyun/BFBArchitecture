package com.bfb.business.vehicle.service;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Business service for vehicle management.
 */
@Service
public class VehicleService {

    public List<Vehicle> findAll() {
        // Placeholder implementation
        return new ArrayList<>();
    }

    public VehicleStatus getStatus(UUID vehicleId) {
        // Placeholder implementation
        return VehicleStatus.AVAILABLE;
    }
}
