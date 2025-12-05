package com.bfb.business.contract.validation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractValidationChain {

    private final List<ContractValidator> validators;

    public ContractValidationChain(
            DateValidator dateValidator,
            ClientExistenceValidator clientExistenceValidator,
            VehicleAvailabilityValidator vehicleAvailabilityValidator,
            OverlapValidator overlapValidator) {
        this.validators = List.of(
            dateValidator,
            clientExistenceValidator,
            vehicleAvailabilityValidator,
            overlapValidator
        );
    }

    public void validateAll(ContractCreationContext context) {
        validators.forEach(validator -> validator.validate(context));
    }
}
