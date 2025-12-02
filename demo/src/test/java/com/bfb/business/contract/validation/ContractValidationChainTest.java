package com.bfb.business.contract.validation;

import com.bfb.business.client.service.ClientService;
import com.bfb.business.contract.exception.*;
import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.service.ContractRepository;
import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.vehicle.service.VehicleService;
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
 * Unit tests for ContractValidationChain.
 * Tests the orchestration of multiple validators.
 */
@ExtendWith(MockitoExtension.class)
class ContractValidationChainTest {

    @Mock
    private ClientService clientService;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private ContractRepository contractRepository;

    private ContractValidationChain validationChain;
    private UUID clientId;
    private UUID vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        DateValidator dateValidator = new DateValidator();
        ClientExistenceValidator clientExistenceValidator = new ClientExistenceValidator(clientService);
        VehicleAvailabilityValidator vehicleAvailabilityValidator = new VehicleAvailabilityValidator(vehicleService);
        OverlapValidator overlapValidator = new OverlapValidator(contractRepository);

        validationChain = new ContractValidationChain(
            dateValidator,
            clientExistenceValidator,
            vehicleAvailabilityValidator,
            overlapValidator
        );

        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(7);
    }

    @Test
    void validateAll_AllValidationsPass_NoException() {
        // Given
        when(clientService.exists(clientId)).thenReturn(true);
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.AVAILABLE);
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validationChain.validateAll(context));
        
        // Verify all validators were called
        verify(clientService).exists(clientId);
        verify(vehicleService).getStatus(vehicleId);
        verify(contractRepository).findOverlappingContracts(vehicleId, startDate, endDate);
    }

    @Test
    void validateAll_InvalidDates_StopsAtFirstValidator() {
        // Given - invalid dates
        LocalDate invalidStartDate = LocalDate.now().plusDays(10);
        LocalDate invalidEndDate = LocalDate.now().plusDays(5);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, invalidStartDate, invalidEndDate);

        // When & Then
        assertThrows(ValidationException.class, () -> validationChain.validateAll(context));
        
        // Verify subsequent validators were NOT called (fail-fast)
        verifyNoInteractions(clientService, vehicleService, contractRepository);
    }

    @Test
    void validateAll_ClientNotFound_StopsAtSecondValidator() {
        // Given
        when(clientService.exists(clientId)).thenReturn(false);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertThrows(ClientUnknownException.class, () -> validationChain.validateAll(context));
        
        // Verify date validation passed but subsequent validators were NOT called
        verify(clientService).exists(clientId);
        verifyNoInteractions(vehicleService, contractRepository);
    }

    @Test
    void validateAll_VehicleBroken_StopsAtThirdValidator() {
        // Given
        when(clientService.exists(clientId)).thenReturn(true);
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.BROKEN);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertThrows(VehicleUnavailableException.class, () -> validationChain.validateAll(context));
        
        // Verify first two validations passed but overlap check was NOT called
        verify(clientService).exists(clientId);
        verify(vehicleService).getStatus(vehicleId);
        verifyNoInteractions(contractRepository);
    }

    @Test
    void validateAll_OverlappingContracts_StopsAtFourthValidator() {
        // Given
        when(clientService.exists(clientId)).thenReturn(true);
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.AVAILABLE);
        Contract existingContract = new Contract(UUID.randomUUID(), clientId, vehicleId, 
            startDate, endDate, com.bfb.business.contract.model.ContractStatus.PENDING);
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(List.of(existingContract));
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertThrows(OverlapException.class, () -> validationChain.validateAll(context));
        
        // Verify all validators were called
        verify(clientService).exists(clientId);
        verify(vehicleService).getStatus(vehicleId);
        verify(contractRepository).findOverlappingContracts(vehicleId, startDate, endDate);
    }

    @Test
    void validateAll_ExecutionOrder_CorrectSequence() {
        // Given - track invocation order
        when(clientService.exists(clientId)).thenReturn(true);
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.AVAILABLE);
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(Collections.emptyList());
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When
        validationChain.validateAll(context);

        // Then - verify order: date -> client -> vehicle -> overlap
        var inOrder = inOrder(clientService, vehicleService, contractRepository);
        inOrder.verify(clientService).exists(clientId);
        inOrder.verify(vehicleService).getStatus(vehicleId);
        inOrder.verify(contractRepository).findOverlappingContracts(vehicleId, startDate, endDate);
    }
}
