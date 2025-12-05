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

    public void cancel() {
        this.status = this.status.transitionTo(ContractStatus.CANCELLED);
    }

    public void markLate() {
        this.status = this.status.transitionTo(ContractStatus.LATE);
    }

    public boolean isOccupying() {
        return status == ContractStatus.PENDING 
            || status == ContractStatus.IN_PROGRESS 
            || status == ContractStatus.LATE;
    }
}
