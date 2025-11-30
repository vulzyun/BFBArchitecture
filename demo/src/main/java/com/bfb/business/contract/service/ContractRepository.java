package com.bfb.business.contract.service;

import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    Page<Contract> findByCriteria(UUID clientId, UUID vehicleId, ContractStatus status, Pageable pageable);
    List<Contract> findOverlappingContracts(UUID vehicleId, LocalDate startDate, LocalDate endDate);
    List<Contract> findByStatus(ContractStatus status);
    List<Contract> findByVehicleIdAndStatus(UUID vehicleId, ContractStatus status);
    
    /**
     * Finds all contracts in a given status with end date before the specified date.
     * Optimized query for finding overdue contracts.
     * 
     * @param status the contract status to filter by
     * @param date the date to compare end dates against
     * @return list of contracts matching the criteria
     */
    List<Contract> findOverdueContracts(ContractStatus status, LocalDate date);
}
