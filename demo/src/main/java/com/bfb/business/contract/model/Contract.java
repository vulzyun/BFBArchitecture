package com.bfb.business.contract.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain model representing a rental contract.
 * Contains business logic and state transitions.
 */
public class Contract {

    private UUID id;
    private UUID clientId;
    private UUID vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;

    public Contract() {
    }

    public Contract(UUID id, UUID clientId, UUID vehicleId, LocalDate startDate, LocalDate endDate, ContractStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters and Setters
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

    // Business methods for state transitions
    public void start() {
        if (!Rules.isTransitionAllowed(this.status, ContractStatus.IN_PROGRESS)) {
            throw new IllegalStateException(
                String.format("Cannot start a contract in status %s", this.status)
            );
        }
        this.status = ContractStatus.IN_PROGRESS;
    }

    public void terminate() {
        if (!Rules.isTransitionAllowed(this.status, ContractStatus.COMPLETED)) {
            throw new IllegalStateException(
                String.format("Cannot terminate a contract in status %s", this.status)
            );
        }
        this.status = ContractStatus.COMPLETED;
    }

    public void cancel() {
        if (!Rules.isTransitionAllowed(this.status, ContractStatus.CANCELLED)) {
            throw new IllegalStateException(
                String.format("Cannot cancel a contract in status %s", this.status)
            );
        }
        this.status = ContractStatus.CANCELLED;
    }

    public void markLate() {
        if (!Rules.isTransitionAllowed(this.status, ContractStatus.LATE)) {
            throw new IllegalStateException(
                String.format("Cannot mark late a contract in status %s", this.status)
            );
        }
        this.status = ContractStatus.LATE;
    }

    /**
     * Checks if the contract is actively occupying the vehicle.
     */
    public boolean isOccupying() {
        return status == ContractStatus.PENDING 
            || status == ContractStatus.IN_PROGRESS 
            || status == ContractStatus.LATE;
    }
}
