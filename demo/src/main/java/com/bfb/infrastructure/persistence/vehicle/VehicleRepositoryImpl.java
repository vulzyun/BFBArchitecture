package com.bfb.infrastructure.persistence.vehicle;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.vehicle.service.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class VehicleRepositoryImpl implements VehicleRepository {

    private final VehicleJpaRepository jpaRepository;

    public VehicleRepositoryImpl(VehicleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = toEntity(vehicle);
        VehicleEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Vehicle> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Vehicle> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
            .map(this::toDomain);
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return jpaRepository.findByStatus(status)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable) {
        return jpaRepository.findByStatus(status, pageable)
            .map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    private VehicleEntity toEntity(Vehicle vehicle) {
        return new VehicleEntity(
            vehicle.getId(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getMotorization(),
            vehicle.getColor(),
            vehicle.getRegistrationPlate(),
            vehicle.getPurchaseDate(),
            vehicle.getStatus()
        );
    }

    private Vehicle toDomain(VehicleEntity entity) {
        return new Vehicle(
            entity.getId(),
            entity.getBrand(),
            entity.getModel(),
            entity.getMotorization(),
            entity.getColor(),
            entity.getRegistrationPlate(),
            entity.getPurchaseDate(),
            entity.getStatus()
        );
    }
}
