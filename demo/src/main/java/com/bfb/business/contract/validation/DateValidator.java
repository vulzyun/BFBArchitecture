package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.ValidationException;
import org.springframework.stereotype.Component;

/**
 * Validates that the start date is before the end date.
 */
@Component
public class DateValidator implements ContractValidator {

    @Override
    public void validate(ContractCreationContext context) {
        if (!context.getStartDate().isBefore(context.getEndDate())) {
            throw new ValidationException(
                String.format("Start date (%s) must be before end date (%s)", 
                    context.getStartDate(), context.getEndDate())
            );
        }
    }
}
