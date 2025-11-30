package com.bfb.business.contract.model;

import com.bfb.business.contract.exception.TransitionNotAllowedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Contract domain model.
 */
class ContractTest {

    @Test
    void testStartFromPending() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ContractStatus.PENDING
        );
        
        // When
        contract.start();
        
        // Then
        assertEquals(ContractStatus.IN_PROGRESS, contract.getStatus());
    }

    @Test
    void testStartFromInProgressThrowsException() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ContractStatus.IN_PROGRESS
        );
        
        // When & Then
        assertThrows(TransitionNotAllowedException.class, contract::start);
    }

    @Test
    void testTerminateFromInProgress() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ContractStatus.IN_PROGRESS
        );
        
        // When
        contract.terminate();
        
        // Then
        assertEquals(ContractStatus.COMPLETED, contract.getStatus());
    }

    @Test
    void testTerminateFromLate() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(3),
            ContractStatus.LATE
        );
        
        // When
        contract.terminate();
        
        // Then
        assertEquals(ContractStatus.COMPLETED, contract.getStatus());
    }

    @Test
    void testTerminateFromPendingThrowsException() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ContractStatus.PENDING
        );
        
        // When & Then
        assertThrows(TransitionNotAllowedException.class, contract::terminate);
    }

    @Test
    void testCancelFromPending() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ContractStatus.PENDING
        );
        
        // When
        contract.cancel();
        
        // Then
        assertEquals(ContractStatus.CANCELLED, contract.getStatus());
    }

    @Test
    void testCancelFromInProgressThrowsException() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ContractStatus.IN_PROGRESS
        );
        
        // When & Then
        assertThrows(TransitionNotAllowedException.class, contract::cancel);
    }

    @Test
    void testMarkLateFromInProgress() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(3),
            ContractStatus.IN_PROGRESS
        );
        
        // When
        contract.markLate();
        
        // Then
        assertEquals(ContractStatus.LATE, contract.getStatus());
    }

    @Test
    void testMarkLateFromPendingThrowsException() {
        // Given
        Contract contract = new Contract(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            ContractStatus.PENDING
        );
        
        // When & Then
        assertThrows(TransitionNotAllowedException.class, contract::markLate);
    }
}
