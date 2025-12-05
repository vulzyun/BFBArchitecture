package com.bfb.business.contract.service;

import com.bfb.business.contract.exception.*;
import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.contract.validation.ContractCreationContext;
import com.bfb.business.contract.validation.ContractValidationChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractValidationChain validationChain;

    public ContractService(
            ContractRepository contractRepository,
            ContractValidationChain validationChain) {
        this.contractRepository = contractRepository;
        this.validationChain = validationChain;
    }

    public Contract create(UUID clientId, UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);
        validationChain.validateAll(context);
        return createAndSaveContract(clientId, vehicleId, startDate, endDate);
    }
    
    private Contract createAndSaveContract(UUID clientId, UUID vehicleId, 
                                          LocalDate startDate, LocalDate endDate) {
        Contract contract = new Contract(null, clientId, vehicleId, startDate, endDate, 
                                        ContractStatus.PENDING);
        return contractRepository.save(contract);
    }

    public Contract start(UUID contractId) {
        Contract contract = findByIdOrThrow(contractId);
        contract.start();
        return contractRepository.save(contract);
    }

    public Contract terminate(UUID contractId) {
        Contract contract = findByIdOrThrow(contractId);
        contract.terminate();
        return contractRepository.save(contract);
    }

    public Contract cancel(UUID contractId) {
        Contract contract = findByIdOrThrow(contractId);
        contract.cancel();
        return contractRepository.save(contract);
    }

    public int markLateIfOverdue() {
        LocalDate today = LocalDate.now();
        
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

    private Contract findByIdOrThrow(UUID id) {
        return contractRepository.findById(id)
            .orElseThrow(() -> new ContractNotFoundException(
                String.format("Contract %s not found", id)
            ));
    }
}
