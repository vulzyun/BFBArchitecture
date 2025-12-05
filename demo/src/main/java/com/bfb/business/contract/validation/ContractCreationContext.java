package com.bfb.business.contract.validation;

import java.time.LocalDate;
import java.util.UUID;

public class ContractCreationContext {
    private final UUID clientId;
    private final UUID vehicleId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ContractCreationContext(UUID clientId, UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        this.clientId = clientId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
