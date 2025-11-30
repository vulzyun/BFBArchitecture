package com.bfb.business.vehicle.service;

import com.bfb.business.vehicle.exception.VehicleNotFoundException;
import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business service for vehicle management.
 */
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle create(String brand, String model) {
        Vehicle vehicle = new Vehicle(null, brand, model, VehicleStatus.AVAILABLE);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle findById(UUID id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException(
                String.format("Vehicle %s not found", id)
            ));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Vehicle> findByStatus(VehicleStatus status, org.springframework.data.domain.Pageable pageable) {
        return vehicleRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Vehicle> findAll(org.springframework.data.domain.Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    public VehicleStatus getStatus(UUID vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        return vehicle.getStatus();
    }

    public Vehicle markAsBroken(UUID vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        vehicle.setStatus(VehicleStatus.BROKEN);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle markAsAvailable(UUID vehicleId) {
        Vehicle vehicle = findById(vehicleId);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        return vehicleRepository.save(vehicle);
    }

    public void delete(UUID id) {
        if (!vehicleRepository.existsById(id)) {
            throw new VehicleNotFoundException(
                String.format("Vehicle %s not found", id)
            );
        }
        vehicleRepository.deleteById(id);
    }
}
