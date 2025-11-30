package com.bfb.infrastructure.persistence.vehicle;

import com.bfb.business.vehicle.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Vehicle.
 */
public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, UUID> {
    List<VehicleEntity> findByStatus(VehicleStatus status);
}
