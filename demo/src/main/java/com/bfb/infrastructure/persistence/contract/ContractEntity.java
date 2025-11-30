package com.bfb.infrastructure.persistence.contract;

import com.bfb.business.contract.model.ContractStatus;
import com.bfb.infrastructure.persistence.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity for contract persistence.
 * Separated from domain model for infrastructure isolation.
 * Extends BaseEntity for automatic audit field management (createdAt, updatedAt).
 */
@Entity
@Table(name = "contracts", indexes = {
    @Index(name = "idx_vehicle_dates", columnList = "vehicle_id, start_date, end_date"),
    @Index(name = "idx_status", columnList = "status")
})
public class ContractEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContractStatus status;

    public ContractEntity() {
    }

    public ContractEntity(UUID id, UUID clientId, UUID vehicleId, LocalDate startDate, LocalDate endDate, ContractStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }
}
