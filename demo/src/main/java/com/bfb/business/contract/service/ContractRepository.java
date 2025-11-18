package com.bfb.business.contract.service;

import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Contract persistence operations.
 * Defined in business layer, implemented in infrastructure layer.
 */
public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(UUID id);
    List<Contract> findByCriteria(UUID clientId, UUID vehicleId, ContractStatus status);
    List<Contract> findOverlappingContracts(UUID vehicleId, LocalDate startDate, LocalDate endDate);
    List<Contract> findByStatus(ContractStatus status);
    List<Contract> findByVehicleIdAndStatus(UUID vehicleId, ContractStatus status);
}
