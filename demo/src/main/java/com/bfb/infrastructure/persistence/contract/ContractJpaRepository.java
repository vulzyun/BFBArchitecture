package com.bfb.infrastructure.persistence.contract;

import com.bfb.business.contract.model.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for contract database access.
 */
@Repository
public interface ContractJpaRepository extends JpaRepository<ContractEntity, UUID> {

    List<ContractEntity> findByVehicleIdAndStatus(UUID vehicleId, ContractStatus status);
    
    @Query("SELECT c FROM ContractEntity c WHERE c.vehicleId = :vehicleId " +
           "AND c.status IN ('PENDING', 'IN_PROGRESS', 'LATE') " +
           "AND NOT (c.endDate < :startDate OR c.startDate > :endDate)")
    List<ContractEntity> findOverlappingContracts(
        @Param("vehicleId") UUID vehicleId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<ContractEntity> findByStatus(ContractStatus status);

    @Query("SELECT c FROM ContractEntity c WHERE " +
           "(:clientId IS NULL OR c.clientId = :clientId) AND " +
           "(:vehicleId IS NULL OR c.vehicleId = :vehicleId) AND " +
           "(:status IS NULL OR c.status = :status)")
    List<ContractEntity> findByCriteria(
        @Param("clientId") UUID clientId,
        @Param("vehicleId") UUID vehicleId,
        @Param("status") ContractStatus status
    );

    @Query("SELECT c FROM ContractEntity c WHERE " +
           "(:clientId IS NULL OR c.clientId = :clientId) AND " +
           "(:vehicleId IS NULL OR c.vehicleId = :vehicleId) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<ContractEntity> findByCriteria(
        @Param("clientId") UUID clientId,
        @Param("vehicleId") UUID vehicleId,
        @Param("status") ContractStatus status,
        Pageable pageable
    );
}
