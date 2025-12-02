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
 * Unit tests for @AdultAge custom validation annotation.
 */
class AdultAgeValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_AdultAge_Valid() {
        // Given - person born 25 years ago
        TestPerson person = new TestPerson(LocalDate.now().minusYears(25));

        // When
        Set<ConstraintViolation<TestPerson>> violations = validator.validate(person);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_ExactlyEighteenYearsOld_Valid() {
        // Given - person born exactly 18 years ago today
        TestPerson person = new TestPerson(LocalDate.now().minusYears(18));

        // When
        Set<ConstraintViolation<TestPerson>> violations = validator.validate(person);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_UnderEighteen_Invalid() {
        // Given - person born 17 years ago
        TestPerson person = new TestPerson(LocalDate.now().minusYears(17));

        // When
        Set<ConstraintViolation<TestPerson>> violations = validator.validate(person);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("18 years old"));
    }

    @Test
    void validate_OneDayUnderEighteen_Invalid() {
        // Given - person will turn 18 tomorrow
        TestPerson person = new TestPerson(LocalDate.now().minusYears(18).plusDays(1));

        // When
        Set<ConstraintViolation<TestPerson>> violations = validator.validate(person);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_NullBirthDate_Valid() {
        // Given - null is handled by @NotNull
        TestPerson person = new TestPerson(null);

        // When
        Set<ConstraintViolation<TestPerson>> violations = validator.validate(person);

        // Then - @AdultAge doesn't fail on null
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_CustomMinAge_TwentyOne() {
        // Given - custom minimum age of 21
        TestPersonWithCustomAge person = new TestPersonWithCustomAge(LocalDate.now().minusYears(20));

        // When
        Set<ConstraintViolation<TestPersonWithCustomAge>> violations = validator.validate(person);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.iterator().next().getMessage().contains("21 years old"));
    }

    @Test
    void validate_CustomMinAge_TwentyOneOrOlder_Valid() {
        // Given
        TestPersonWithCustomAge person = new TestPersonWithCustomAge(LocalDate.now().minusYears(21));

        // When
        Set<ConstraintViolation<TestPersonWithCustomAge>> violations = validator.validate(person);

        // Then
        assertTrue(violations.isEmpty());
    }

    // Test class with default @AdultAge (18)
    private static class TestPerson {
        @AdultAge
        private final LocalDate birthDate;

        public TestPerson(LocalDate birthDate) {
            this.birthDate = birthDate;
        }
    }

    // Test class with custom minimum age
    private static class TestPersonWithCustomAge {
        @AdultAge(minAge = 21, message = "Must be at least 21 years old")
        private final LocalDate birthDate;

        public TestPersonWithCustomAge(LocalDate birthDate) {
            this.birthDate = birthDate;
        }
    }
}
