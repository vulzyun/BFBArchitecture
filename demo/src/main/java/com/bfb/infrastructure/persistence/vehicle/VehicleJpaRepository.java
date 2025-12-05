package com.bfb.infrastructure.persistence.vehicle;

import com.bfb.business.vehicle.model.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, UUID> {
    List<VehicleEntity> findByStatus(VehicleStatus status);
    Page<VehicleEntity> findByStatus(VehicleStatus status, Pageable pageable);
    
    boolean existsByRegistrationPlate(String registrationPlate);
}
