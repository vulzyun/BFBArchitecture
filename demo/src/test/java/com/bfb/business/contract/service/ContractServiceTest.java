package com.bfb.business.contract.service;

import com.bfb.business.contract.exception.*;
import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.contract.validation.ContractValidationChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContractService (business layer).
 * Tests business logic without Spring context.
 */
@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractValidationChain validationChain;

    private ContractService contractService;

    private UUID clientId;
    private UUID vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        contractService = new ContractService(contractRepository, validationChain);
        
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(8);
    }

    // ========== CREATE TESTS ==========

    @Test
    void createContract_Success() {
        // Given
        doNothing().when(validationChain).validateAll(any());
        when(contractRepository.save(any(Contract.class)))
            .thenAnswer(invocation -> {
                Contract contract = invocation.getArgument(0);
                contract.setId(UUID.randomUUID());
                return contract;
            });

        // When
        Contract result = contractService.create(clientId, vehicleId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(vehicleId, result.getVehicleId());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertEquals(ContractStatus.PENDING, result.getStatus());
        
        verify(validationChain).validateAll(any());
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void createContract_InvalidDates_ThrowsValidationException() {
        // Given
        LocalDate invalidStartDate = LocalDate.now().plusDays(10);
        LocalDate invalidEndDate = LocalDate.now().plusDays(5);
        doThrow(new ValidationException("Start date must be before end date"))
            .when(validationChain).validateAll(any());

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () ->
            contractService.create(clientId, vehicleId, invalidStartDate, invalidEndDate)
        );
        
        assertNotNull(exception);
        verify(validationChain).validateAll(any());
        verifyNoInteractions(contractRepository);
    }

    @Test
    void createContract_ClientNotFound_ThrowsClientUnknownException() {
        // Given
        doThrow(new ClientUnknownException("Client not found"))
            .when(validationChain).validateAll(any());

        // When & Then
        ClientUnknownException exception = assertThrows(ClientUnknownException.class, () ->
            contractService.create(clientId, vehicleId, startDate, endDate)
        );
        
        assertNotNull(exception);
        verify(validationChain).validateAll(any());
        verifyNoInteractions(contractRepository);
    }

    @Test
    void createContract_VehicleBroken_ThrowsVehicleUnavailableException() {
        // Given
        doThrow(new VehicleUnavailableException("Vehicle is unavailable"))
            .when(validationChain).validateAll(any());

        // When & Then
        VehicleUnavailableException exception = assertThrows(VehicleUnavailableException.class, () ->
            contractService.create(clientId, vehicleId, startDate, endDate)
        );
        
        assertNotNull(exception);
        verify(validationChain).validateAll(any());
        verifyNoInteractions(contractRepository);
    }

    @Test
    void createContract_OverlappingContract_ThrowsOverlapException() {
        // Given
        doThrow(new OverlapException("Overlapping contract exists"))
            .when(validationChain).validateAll(any());

        // When & Then
        OverlapException exception = assertThrows(OverlapException.class, () ->
            contractService.create(clientId, vehicleId, startDate, endDate)
        );
        
        assertNotNull(exception);
        verify(validationChain).validateAll(any());
        verify(contractRepository, never()).save(any());
    }

    // ========== START TESTS ==========

    @Test
    void startContract_Success() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.PENDING);
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Contract result = contractService.start(contractId);

        // Then
        assertEquals(ContractStatus.IN_PROGRESS, result.getStatus());
        verify(contractRepository).findById(contractId);
        verify(contractRepository).save(contract);
    }

    @Test
    void startContract_NotFound_ThrowsContractNotFoundException() {
        // Given
        UUID contractId = UUID.randomUUID();
        when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

        // When & Then
        ContractNotFoundException exception = assertThrows(ContractNotFoundException.class, () ->
            contractService.start(contractId)
        );
        
        assertNotNull(exception);
    }

    @Test
    void startContract_AlreadyInProgress_ThrowsTransitionNotAllowedException() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.IN_PROGRESS);
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // When & Then
        TransitionNotAllowedException exception = assertThrows(TransitionNotAllowedException.class, () ->
            contractService.start(contractId)
        );
        
        assertNotNull(exception);
    }

    // ========== TERMINATE TESTS ==========

    @Test
    void terminateContract_Success() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.IN_PROGRESS);
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Contract result = contractService.terminate(contractId);

        // Then
        assertEquals(ContractStatus.COMPLETED, result.getStatus());
        verify(contractRepository).save(contract);
    }

    @Test
    void terminateContract_FromPending_ThrowsTransitionNotAllowedException() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.PENDING);
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // When & Then
        TransitionNotAllowedException exception = assertThrows(TransitionNotAllowedException.class, () ->
            contractService.terminate(contractId)
        );
        
        assertNotNull(exception);
    }

    // ========== CANCEL TESTS ==========

    @Test
    void cancelContract_Success() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.PENDING);
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Contract result = contractService.cancel(contractId);

        // Then
        assertEquals(ContractStatus.CANCELLED, result.getStatus());
        verify(contractRepository).save(contract);
    }

    @Test
    void cancelContract_FromInProgress_ThrowsTransitionNotAllowedException() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.IN_PROGRESS);
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // When & Then
        TransitionNotAllowedException exception = assertThrows(TransitionNotAllowedException.class, () ->
            contractService.cancel(contractId)
        );
        
        assertNotNull(exception);
    }

    // ========== MARK LATE TESTS ==========

    @Test
    void markLateIfOverdue_MarksOverdueContracts() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(5);
        
        Contract overdueContract1 = new Contract(
            UUID.randomUUID(), clientId, vehicleId, 
            LocalDate.now().minusDays(10), pastDate, ContractStatus.IN_PROGRESS
        );
        Contract overdueContract2 = new Contract(
            UUID.randomUUID(), clientId, UUID.randomUUID(), 
            LocalDate.now().minusDays(8), pastDate, ContractStatus.IN_PROGRESS
        );
        
        // Mock the new optimized query method
        when(contractRepository.findOverdueContracts(eq(ContractStatus.IN_PROGRESS), any(LocalDate.class)))
            .thenReturn(List.of(overdueContract1, overdueContract2));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int count = contractService.markLateIfOverdue();

        // Then
        assertEquals(2, count);
        assertEquals(ContractStatus.LATE, overdueContract1.getStatus());
        assertEquals(ContractStatus.LATE, overdueContract2.getStatus());
        verify(contractRepository, times(2)).save(any(Contract.class));
    }

    @Test
    void markLateIfOverdue_NoOverdueContracts() {
        // Given - mock the new optimized query method
        when(contractRepository.findOverdueContracts(eq(ContractStatus.IN_PROGRESS), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        // When
        int count = contractService.markLateIfOverdue();

        // Then
        assertEquals(0, count);
        verify(contractRepository, never()).save(any());
    }

    // ========== CANCEL PENDING FOR VEHICLE TESTS ==========

    @Test
    void cancelPendingContractsForVehicle_Success() {
        // Given
        Contract pending1 = new Contract(
            UUID.randomUUID(), clientId, vehicleId, startDate, endDate, ContractStatus.PENDING
        );
        Contract pending2 = new Contract(
            UUID.randomUUID(), UUID.randomUUID(), vehicleId, startDate, endDate, ContractStatus.PENDING
        );
        
        when(contractRepository.findByVehicleIdAndStatus(vehicleId, ContractStatus.PENDING))
            .thenReturn(List.of(pending1, pending2));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int count = contractService.cancelPendingContractsForVehicle(vehicleId);

        // Then
        assertEquals(2, count);
        assertEquals(ContractStatus.CANCELLED, pending1.getStatus());
        assertEquals(ContractStatus.CANCELLED, pending2.getStatus());
        verify(contractRepository, times(2)).save(any(Contract.class));
    }

    @Test
    void cancelPendingContractsForVehicle_NoPendingContracts() {
        // Given
        when(contractRepository.findByVehicleIdAndStatus(vehicleId, ContractStatus.PENDING))
            .thenReturn(Collections.emptyList());

        // When
        int count = contractService.cancelPendingContractsForVehicle(vehicleId);

        // Then
        assertEquals(0, count);
        verify(contractRepository, never()).save(any());
    }

    // ========== FIND TESTS ==========

    @Test
    void findById_Success() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.PENDING);
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // When
        Contract result = contractService.findById(contractId);

        // Then
        assertNotNull(result);
        assertEquals(contractId, result.getId());
    }

    @Test
    void findById_NotFound_ThrowsContractNotFoundException() {
        // Given
        UUID contractId = UUID.randomUUID();
        when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

        // When & Then
        ContractNotFoundException exception = assertThrows(ContractNotFoundException.class, () ->
            contractService.findById(contractId)
        );
        
        assertNotNull(exception);
    }

    @Test
    void findByCriteria_Success() {
        // Given
        List<Contract> contracts = List.of(
            new Contract(UUID.randomUUID(), clientId, vehicleId, startDate, endDate, ContractStatus.PENDING)
        );
        when(contractRepository.findByCriteria(clientId, vehicleId, ContractStatus.PENDING))
            .thenReturn(contracts);

        // When
        List<Contract> result = contractService.findByCriteria(clientId, vehicleId, ContractStatus.PENDING);

        // Then
        assertEquals(1, result.size());
        verify(contractRepository).findByCriteria(clientId, vehicleId, ContractStatus.PENDING);
    }
}
