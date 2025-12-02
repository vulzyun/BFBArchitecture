package com.bfb.interfaces.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure start date is before end date.
 * Can be applied at the class/record level for cross-field validation.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    
    String message() default "Start date must be before end date";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Name of the start date field.
     */
    String startDate() default "startDate";
    
    /**
     * Name of the end date field.
     */
    String endDate() default "endDate";
}
