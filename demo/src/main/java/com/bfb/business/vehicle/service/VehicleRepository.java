package com.bfb.business.vehicle.service;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Vehicle persistence operations.
 * Defined in business layer, implemented in infrastructure layer.
 */
public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID id);
    List<Vehicle> findAll();
    List<Vehicle> findByStatus(VehicleStatus status);
    void deleteById(UUID id);
}
