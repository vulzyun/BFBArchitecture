package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DateValidator.
 */
class DateValidatorTest {

    private DateValidator validator;
    private UUID clientId;
    private UUID vehicleId;

    @BeforeEach
    void setUp() {
        validator = new DateValidator();
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
    }

    @Test
    void validate_ValidDates_NoException() {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(7);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
    }

    @Test
    void validate_StartDateAfterEndDate_ThrowsValidationException() {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(5);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> validator.validate(context));
        
        assertTrue(exception.getMessage().contains("must be before"));
        assertTrue(exception.getMessage().contains(startDate.toString()));
        assertTrue(exception.getMessage().contains(endDate.toString()));
    }

    @Test
    void validate_StartDateEqualsEndDate_ThrowsValidationException() {
        // Given
        LocalDate sameDate = LocalDate.now().plusDays(5);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, sameDate, sameDate);

        // When & Then
        assertThrows(ValidationException.class, () -> validator.validate(context));
    }

    @Test
    void validate_OneDayDifference_NoException() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 10);
        LocalDate endDate = LocalDate.of(2025, 12, 11);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
    }

    @Test
    void validate_LongPeriod_NoException() {
        // Given - 1 year rental
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
    }
}
