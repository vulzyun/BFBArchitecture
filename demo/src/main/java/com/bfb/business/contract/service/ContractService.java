package com.bfb.business.contract.service;

import com.bfb.business.contract.exception.*;
import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.contract.model.Rules;
import com.bfb.business.vehicle.model.VehicleStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Business service for contract management.
 * Implements all business rules and validations.
 */
@Service
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final VehicleStatusPort vehicleStatusPort;
    private final ClientExistencePort clientExistencePort;

    public ContractService(
            ContractRepository contractRepository,
            VehicleStatusPort vehicleStatusPort,
            ClientExistencePort clientExistencePort) {
        this.contractRepository = contractRepository;
        this.vehicleStatusPort = vehicleStatusPort;
        this.clientExistencePort = clientExistencePort;
    }

    /**
     * Creates a new contract with comprehensive validation.
     * Validates dates, client existence, vehicle availability, and checks for scheduling conflicts.
     * 
     * @param clientId the client ID
     * @param vehicleId the vehicle ID
     * @param startDate the rental start date
     * @param endDate the rental end date
     * @return the created contract with PENDING status
     * @throws ValidationException if dates are invalid
     * @throws ClientUnknownException if client doesn't exist
     * @throws VehicleUnavailableException if vehicle is broken
     * @throws OverlapException if dates overlap with existing contracts
     */
    public Contract create(UUID clientId, UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        validateClientExists(clientId);
        validateVehicleAvailable(vehicleId);
        validateNoOverlap(vehicleId, startDate, endDate);
        
        return createAndSaveContract(clientId, vehicleId, startDate, endDate);
    }
    
    /**
     * Validates that start date is before end date.
     */
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new ValidationException(
                String.format("Start date (%s) must be before end date (%s)", startDate, endDate)
            );
        }
    }
    
    /**
     * Validates that the client exists in the system.
     */
    private void validateClientExists(UUID clientId) {
        if (!clientExistencePort.existsById(clientId)) {
            throw new ClientUnknownException(
                String.format("Client with ID '%s' not found. Ensure the client exists before creating a contract.", 
                             clientId)
            );
        }
    }
    
    /**
     * Validates that the vehicle is available for rental (not broken).
     */
    private void validateVehicleAvailable(UUID vehicleId) {
        VehicleStatus vehicleStatus = vehicleStatusPort.getStatus(vehicleId);
        if (vehicleStatus == VehicleStatus.BROKEN) {
            throw new VehicleUnavailableException(
                String.format("Vehicle '%s' is currently broken and cannot be rented. " +
                             "Please choose another vehicle or wait for repairs.", vehicleId)
            );
        }
    }
    
    /**
     * Validates that no other contracts overlap with the requested period.
     */
    private void validateNoOverlap(UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        List<Contract> overlappingContracts = contractRepository.findOverlappingContracts(
            vehicleId, startDate, endDate
        );
        
        if (!overlappingContracts.isEmpty()) {
            String conflictingIds = overlappingContracts.stream()
                .map(c -> c.getId().toString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("unknown");
                
            throw new OverlapException(
                String.format("Cannot create contract: Vehicle '%s' is already booked during %s to %s. " +
                             "Conflicting contract IDs: %s",
                             vehicleId, startDate, endDate, conflictingIds)
            );
        }
    }
    
    /**
     * Creates and persists a new contract with PENDING status.
     */
    private Contract createAndSaveContract(UUID clientId, UUID vehicleId, 
                                          LocalDate startDate, LocalDate endDate) {
        Contract contract = new Contract(null, clientId, vehicleId, startDate, endDate, 
                                        ContractStatus.PENDING);
        return contractRepository.save(contract);
    }

    public Contract start(UUID contractId) {
        Contract contract = findByIdOrThrow(contractId);
        
        if (!Rules.isTransitionAllowed(contract.getStatus(), ContractStatus.IN_PROGRESS)) {
            throw new TransitionNotAllowedException(
                String.format("Cannot start a contract in status %s", contract.getStatus())
            );
        }
        
        contract.start();
        return contractRepository.save(contract);
    }

    public Contract terminate(UUID contractId) {
        Contract contract = findByIdOrThrow(contractId);
        
        if (!Rules.isTransitionAllowed(contract.getStatus(), ContractStatus.COMPLETED)) {
            throw new TransitionNotAllowedException(
                String.format("Cannot terminate a contract in status %s", contract.getStatus())
            );
        }
        
        contract.terminate();
        return contractRepository.save(contract);
    }

    public Contract cancel(UUID contractId) {
        Contract contract = findByIdOrThrow(contractId);
        
        if (!Rules.isTransitionAllowed(contract.getStatus(), ContractStatus.CANCELLED)) {
            throw new TransitionNotAllowedException(
                String.format("Cannot cancel a contract in status %s", contract.getStatus())
            );
        }
        
        contract.cancel();
        return contractRepository.save(contract);
    }

    /**
     * Marks all overdue contracts as LATE.
     * Uses optimized query to find only contracts that are IN_PROGRESS and past their end date.
     * 
     * @return the number of contracts marked as late
     */
    public int markLateIfOverdue() {
        LocalDate today = LocalDate.now();
        
        // ✅ OPTIMIZED: Only load contracts that are actually overdue
        List<Contract> overdueContracts = contractRepository.findOverdueContracts(
            ContractStatus.IN_PROGRESS, 
            today
        );
        
        int count = 0;
        for (Contract contract : overdueContracts) {
            contract.markLate();
            contractRepository.save(contract);
            count++;
        }
        
        return count;
    }

    public int cancelPendingContractsForVehicle(UUID vehicleId) {
        List<Contract> pendingContracts = contractRepository.findByVehicleIdAndStatus(
            vehicleId, 
            ContractStatus.PENDING
        );
        
        int count = 0;
        for (Contract contract : pendingContracts) {
            contract.cancel();
            contractRepository.save(contract);
            count++;
        }
        
        return count;
    }

    @Transactional(readOnly = true)
    public Contract findById(UUID id) {
        return findByIdOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Contract> findByCriteria(UUID clientId, UUID vehicleId, ContractStatus status) {
        return contractRepository.findByCriteria(clientId, vehicleId, status);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Contract> findByCriteria(
            UUID clientId, 
            UUID vehicleId, 
            ContractStatus status, 
            org.springframework.data.domain.Pageable pageable) {
        return contractRepository.findByCriteria(clientId, vehicleId, status, pageable);
    }

    // === Helpers ===

    private Contract findByIdOrThrow(UUID id) {
        return contractRepository.findById(id)
            .orElseThrow(() -> new ContractNotFoundException(
                String.format("Contract %s not found", id)
            ));
    }
}
