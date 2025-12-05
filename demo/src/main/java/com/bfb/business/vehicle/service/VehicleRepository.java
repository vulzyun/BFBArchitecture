package com.bfb.business.vehicle.service;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID id);
    List<Vehicle> findAll();
    Page<Vehicle> findAll(Pageable pageable);
    List<Vehicle> findByStatus(VehicleStatus status);
    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    
    boolean existsByRegistrationPlate(String registrationPlate);
}
