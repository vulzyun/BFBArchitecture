package com.bfb.business.contract.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for business rules validation.
 */
class RulesTest {

    @ParameterizedTest
    @CsvSource({
        "PENDING, IN_PROGRESS, true",
        "PENDING, CANCELLED, true",
        "PENDING, COMPLETED, false",
        "PENDING, LATE, false",
        "IN_PROGRESS, COMPLETED, true",
        "IN_PROGRESS, LATE, true",
        "IN_PROGRESS, PENDING, false",
        "IN_PROGRESS, CANCELLED, false",
        "LATE, COMPLETED, true",
        "LATE, PENDING, false",
        "LATE, IN_PROGRESS, false",
        "LATE, CANCELLED, false",
        "COMPLETED, PENDING, false",
        "COMPLETED, IN_PROGRESS, false",
        "COMPLETED, LATE, false",
        "COMPLETED, CANCELLED, false",
        "CANCELLED, PENDING, false",
        "CANCELLED, IN_PROGRESS, false",
        "CANCELLED, LATE, false",
        "CANCELLED, COMPLETED, false"
    })
    void testIsTransitionAllowed(String fromStr, String toStr, boolean expected) {
        // Given
        ContractStatus from = ContractStatus.valueOf(fromStr);
        ContractStatus to = ContractStatus.valueOf(toStr);
        
        // When
        boolean result = Rules.isTransitionAllowed(from, to);
        
        // Then
        assertEquals(expected, result,
            String.format("Transition %s -> %s should be %s", from, to, expected ? "allowed" : "forbidden"));
    }

    @Test
    void testPendingAllowedTransitions() {
        assertTrue(Rules.isTransitionAllowed(ContractStatus.PENDING, ContractStatus.IN_PROGRESS));
        assertTrue(Rules.isTransitionAllowed(ContractStatus.PENDING, ContractStatus.CANCELLED));
    }

    @Test
    void testInProgressAllowedTransitions() {
        assertTrue(Rules.isTransitionAllowed(ContractStatus.IN_PROGRESS, ContractStatus.COMPLETED));
        assertTrue(Rules.isTransitionAllowed(ContractStatus.IN_PROGRESS, ContractStatus.LATE));
    }

    @Test
    void testLateAllowedTransitions() {
        assertTrue(Rules.isTransitionAllowed(ContractStatus.LATE, ContractStatus.COMPLETED));
    }

    @Test
    void testCompletedIsTerminal() {
        assertFalse(Rules.isTransitionAllowed(ContractStatus.COMPLETED, ContractStatus.PENDING));
        assertFalse(Rules.isTransitionAllowed(ContractStatus.COMPLETED, ContractStatus.IN_PROGRESS));
        assertFalse(Rules.isTransitionAllowed(ContractStatus.COMPLETED, ContractStatus.LATE));
        assertFalse(Rules.isTransitionAllowed(ContractStatus.COMPLETED, ContractStatus.CANCELLED));
    }

    @Test
    void testCancelledIsTerminal() {
        assertFalse(Rules.isTransitionAllowed(ContractStatus.CANCELLED, ContractStatus.PENDING));
        assertFalse(Rules.isTransitionAllowed(ContractStatus.CANCELLED, ContractStatus.IN_PROGRESS));
        assertFalse(Rules.isTransitionAllowed(ContractStatus.CANCELLED, ContractStatus.LATE));
        assertFalse(Rules.isTransitionAllowed(ContractStatus.CANCELLED, ContractStatus.COMPLETED));
    }
}
