package com.bfb.business.contract.validation;

/**
 * Interface for contract validation rules.
 * Each validator implements a specific validation rule.
 */
public interface ContractValidator {
    /**
     * Validates a contract creation context.
     * @param context the validation context
     * @throws RuntimeException if validation fails
     */
    void validate(ContractCreationContext context);
}
