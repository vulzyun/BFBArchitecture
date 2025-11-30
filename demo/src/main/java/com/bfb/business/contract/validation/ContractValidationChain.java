package com.bfb.business.contract.validation;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validation chain that executes all contract validators in order.
 * Uses the Chain of Responsibility pattern.
 */
@Component
public class ContractValidationChain {

    private final List<ContractValidator> validators;

    public ContractValidationChain(
            DateValidator dateValidator,
            ClientExistenceValidator clientExistenceValidator,
            VehicleAvailabilityValidator vehicleAvailabilityValidator,
            OverlapValidator overlapValidator) {
        // Order matters: fast-fail validations first
        this.validators = List.of(
            dateValidator,
            clientExistenceValidator,
            vehicleAvailabilityValidator,
            overlapValidator
        );
    }

    /**
     * Executes all validators in sequence.
     * Stops at the first validation failure.
     * 
     * @param context the validation context
     * @throws RuntimeException if any validation fails
     */
    public void validateAll(ContractCreationContext context) {
        validators.forEach(validator -> validator.validate(context));
    }
}
