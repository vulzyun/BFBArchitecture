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

    public Contract create(UUID clientId, UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        // 1. Validate dates
        if (!startDate.isBefore(endDate)) {
            throw new ValidationException(
                "Start date must be before end date"
            );
        }

        // 2. Check client exists
        if (!clientExistencePort.existsById(clientId)) {
            throw new ClientUnknownException(
                String.format("Client %s not found", clientId)
            );
        }

        // 3. Check vehicle is not broken
        VehicleStatus vehicleStatus = vehicleStatusPort.getStatus(vehicleId);
        if (vehicleStatus == VehicleStatus.BROKEN) {
            throw new VehicleUnavailableException(
                String.format("Vehicle %s is broken and cannot be rented", vehicleId)
            );
        }

        // 4. Check for overlapping contracts
        List<Contract> overlappingContracts = contractRepository.findOverlappingContracts(
            vehicleId, startDate, endDate
        );
        
        if (!overlappingContracts.isEmpty()) {
            throw new OverlapException(
                String.format("Overlap detected for vehicle %s on period %s - %s",
                    vehicleId, startDate, endDate)
            );
        }

        // 5. Create and save contract
        Contract contract = new Contract(null, clientId, vehicleId, startDate, endDate, ContractStatus.PENDING);
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

    public int markLateIfOverdue() {
        LocalDate today = LocalDate.now();
        List<Contract> inProgressContracts = contractRepository.findByStatus(ContractStatus.IN_PROGRESS);
        
        int count = 0;
        for (Contract contract : inProgressContracts) {
            if (contract.getEndDate().isBefore(today)) {
                contract.markLate();
                contractRepository.save(contract);
                count++;
            }
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
