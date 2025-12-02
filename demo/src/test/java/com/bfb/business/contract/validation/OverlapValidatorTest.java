package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.OverlapException;
import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.contract.service.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OverlapValidator.
 */
@ExtendWith(MockitoExtension.class)
class OverlapValidatorTest {

    @Mock
    private ContractRepository contractRepository;

    private OverlapValidator validator;
    private UUID clientId;
    private UUID vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        validator = new OverlapValidator(contractRepository);
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(7);
    }

    @Test
    void validate_NoOverlappingContracts_NoException() {
        // Given
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
        verify(contractRepository).findOverlappingContracts(vehicleId, startDate, endDate);
    }

    @Test
    void validate_OneOverlappingContract_ThrowsOverlapException() {
        // Given
        UUID conflictingContractId = UUID.randomUUID();
        Contract existingContract = new Contract(
            conflictingContractId, clientId, vehicleId, 
            startDate.plusDays(2), endDate.plusDays(2), 
            ContractStatus.PENDING
        );
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(List.of(existingContract));
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        OverlapException exception = assertThrows(OverlapException.class, 
            () -> validator.validate(context));
        
        assertTrue(exception.getMessage().contains(vehicleId.toString()));
        assertTrue(exception.getMessage().contains(conflictingContractId.toString()));
        assertTrue(exception.getMessage().toLowerCase().contains("booked"));
        verify(contractRepository).findOverlappingContracts(vehicleId, startDate, endDate);
    }

    @Test
    void validate_MultipleOverlappingContracts_ThrowsOverlapExceptionWithAllIds() {
        // Given
        UUID contractId1 = UUID.randomUUID();
        UUID contractId2 = UUID.randomUUID();
        UUID contractId3 = UUID.randomUUID();
        
        List<Contract> overlappingContracts = List.of(
            new Contract(contractId1, clientId, vehicleId, startDate, endDate, ContractStatus.PENDING),
            new Contract(contractId2, clientId, vehicleId, startDate.plusDays(1), endDate.plusDays(1), ContractStatus.IN_PROGRESS),
            new Contract(contractId3, clientId, vehicleId, startDate.plusDays(2), endDate.plusDays(2), ContractStatus.LATE)
        );
        
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(overlappingContracts);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        OverlapException exception = assertThrows(OverlapException.class, 
            () -> validator.validate(context));
        
        String message = exception.getMessage();
        assertTrue(message.contains(contractId1.toString()));
        assertTrue(message.contains(contractId2.toString()));
        assertTrue(message.contains(contractId3.toString()));
    }

    @Test
    void validate_DifferentVehicle_NoOverlapCheck() {
        // Given - different vehicle, so no overlap expected
        UUID differentVehicleId = UUID.randomUUID();
        when(contractRepository.findOverlappingContracts(differentVehicleId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        ContractCreationContext context = new ContractCreationContext(clientId, differentVehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
        verify(contractRepository).findOverlappingContracts(differentVehicleId, startDate, endDate);
        verify(contractRepository, never()).findOverlappingContracts(eq(vehicleId), any(), any());
    }
}
