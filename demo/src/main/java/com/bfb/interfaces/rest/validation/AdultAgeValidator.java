package com.bfb.interfaces.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

/**
 * Validator implementation for @AdultAge annotation.
 * Validates that a birth date indicates the person is at least the minimum age.
 */
public class AdultAgeValidator implements ConstraintValidator<AdultAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(AdultAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true; // Let @NotNull handle null checks
        }

        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();
        
        return age >= minAge;
    }
}
