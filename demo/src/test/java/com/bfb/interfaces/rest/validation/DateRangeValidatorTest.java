package com.bfb.interfaces.rest.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for @ValidDateRange custom validation annotation.
 */
class DateRangeValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_ValidDateRange_NoViolations() {
        // Given - start date before end date
        TestContract contract = new TestContract(
            LocalDate.of(2025, 12, 10),
            LocalDate.of(2025, 12, 20)
        );

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_StartDateAfterEndDate_HasViolation() {
        // Given - start date after end date
        TestContract contract = new TestContract(
            LocalDate.of(2025, 12, 20),
            LocalDate.of(2025, 12, 10)
        );

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("before end date"));
    }

    @Test
    void validate_StartDateEqualsEndDate_HasViolation() {
        // Given - same date
        LocalDate sameDate = LocalDate.of(2025, 12, 15);
        TestContract contract = new TestContract(sameDate, sameDate);

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_OneDayDifference_NoViolations() {
        // Given - one day apart
        TestContract contract = new TestContract(
            LocalDate.of(2025, 12, 10),
            LocalDate.of(2025, 12, 11)
        );

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_NullStartDate_NoViolations() {
        // Given - null start date (let @NotNull handle)
        TestContract contract = new TestContract(null, LocalDate.of(2025, 12, 20));

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then - @ValidDateRange doesn't fail on null
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_NullEndDate_NoViolations() {
        // Given - null end date (let @NotNull handle)
        TestContract contract = new TestContract(LocalDate.of(2025, 12, 10), null);

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_BothDatesNull_NoViolations() {
        // Given - both null
        TestContract contract = new TestContract(null, null);

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_LongPeriod_NoViolations() {
        // Given - 1 year period
        TestContract contract = new TestContract(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2026, 1, 1)
        );

        // When
        Set<ConstraintViolation<TestContract>> violations = validator.validate(contract);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_CustomFieldNames_Works() {
        // Given - custom field names
        TestBooking booking = new TestBooking(
            LocalDate.of(2025, 12, 20),
            LocalDate.of(2025, 12, 10)
        );

        // When
        Set<ConstraintViolation<TestBooking>> violations = validator.validate(booking);

        // Then
        assertFalse(violations.isEmpty());
    }

    // Test class with default field names
    @ValidDateRange
    private static class TestContract {
        private final LocalDate startDate;
        private final LocalDate endDate;

        public TestContract(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    // Test class with custom field names
    @ValidDateRange(startDate = "checkIn", endDate = "checkOut", message = "Check-in must be before check-out")
    private static class TestBooking {
        private final LocalDate checkIn;
        private final LocalDate checkOut;

        public TestBooking(LocalDate checkIn, LocalDate checkOut) {
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }
    }
}
