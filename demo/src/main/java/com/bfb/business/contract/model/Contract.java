package com.bfb.business.contract.model;

import java.time.LocalDate;
import java.util.UUID;

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

    public void start() {
        this.status = this.status.transitionTo(ContractStatus.IN_PROGRESS);
    }

    public void terminate() {
        this.status = this.status.transitionTo(ContractStatus.COMPLETED);
    }

    /**
     * Cancels the contract by transitioning from PENDING to CANCELLED.
     * @throws TransitionNotAllowedException if transition is not allowed
     */
    public void cancel() {
        this.status = this.status.transitionTo(ContractStatus.CANCELLED);
    }

    /**
     * Marks the contract as late by transitioning from IN_PROGRESS to LATE.
     * @throws TransitionNotAllowedException if transition is not allowed
     */
    public void markLate() {
        this.status = this.status.transitionTo(ContractStatus.LATE);
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
