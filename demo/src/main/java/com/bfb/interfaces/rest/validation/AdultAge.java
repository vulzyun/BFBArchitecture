package com.bfb.interfaces.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure a person is at least 18 years old.
 * Applied to LocalDate birthDate fields.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdultAgeValidator.class)
@Documented
public @interface AdultAge {
    
    String message() default "Must be at least 18 years old";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Minimum age required (default 18).
     */
    int minAge() default 18;
}
