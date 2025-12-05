package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.OverlapException;
import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.service.ContractRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OverlapValidator implements ContractValidator {

    private final ContractRepository contractRepository;

    public OverlapValidator(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public void validate(ContractCreationContext context) {
        List<Contract> overlappingContracts = contractRepository.findOverlappingContracts(
            context.getVehicleId(), 
            context.getStartDate(), 
            context.getEndDate()
        );
        
        if (!overlappingContracts.isEmpty()) {
            String conflictingIds = overlappingContracts.stream()
                .map(c -> c.getId().toString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("unknown");
                
            throw new OverlapException(
                String.format("Cannot create contract: Vehicle '%s' is already booked during %s to %s. " +
                    "Conflicting contract IDs: %s",
                    context.getVehicleId(), context.getStartDate(), context.getEndDate(), conflictingIds)
            );
        }
    }
}
