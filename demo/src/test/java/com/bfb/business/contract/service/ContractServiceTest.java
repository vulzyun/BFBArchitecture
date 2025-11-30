package com.bfb.business.contract.service;

import com.bfb.business.contract.exception.*;
import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.vehicle.model.VehicleStatus;
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
    private VehicleStatusPort vehicleStatusPort;

    @Mock
    private ClientExistencePort clientExistencePort;

    private ContractService contractService;

    private UUID clientId;
    private UUID vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        contractService = new ContractService(contractRepository, vehicleStatusPort, clientExistencePort);
        
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(8);
    }

    // ========== CREATE TESTS ==========

    @Test
    void createContract_Success() {
        // Given
        when(clientExistencePort.existsById(clientId)).thenReturn(true);
        when(vehicleStatusPort.getStatus(vehicleId)).thenReturn(VehicleStatus.AVAILABLE);
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(Collections.emptyList());
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
        
        verify(clientExistencePort).existsById(clientId);
        verify(vehicleStatusPort).getStatus(vehicleId);
        verify(contractRepository).findOverlappingContracts(vehicleId, startDate, endDate);
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void createContract_InvalidDates_ThrowsValidationException() {
        // Given
        LocalDate invalidStartDate = LocalDate.now().plusDays(10);
        LocalDate invalidEndDate = LocalDate.now().plusDays(5);

        // When & Then
        assertThrows(ValidationException.class, () ->
            contractService.create(clientId, vehicleId, invalidStartDate, invalidEndDate)
        );
        
        verifyNoInteractions(clientExistencePort, vehicleStatusPort, contractRepository);
    }

    @Test
    void createContract_ClientNotFound_ThrowsClientUnknownException() {
        // Given
        when(clientExistencePort.existsById(clientId)).thenReturn(false);

        // When & Then
        assertThrows(ClientUnknownException.class, () ->
            contractService.create(clientId, vehicleId, startDate, endDate)
        );
        
        verify(clientExistencePort).existsById(clientId);
        verifyNoInteractions(vehicleStatusPort, contractRepository);
    }

    @Test
    void createContract_VehicleBroken_ThrowsVehicleUnavailableException() {
        // Given
        when(clientExistencePort.existsById(clientId)).thenReturn(true);
        when(vehicleStatusPort.getStatus(vehicleId)).thenReturn(VehicleStatus.BROKEN);

        // When & Then
        assertThrows(VehicleUnavailableException.class, () ->
            contractService.create(clientId, vehicleId, startDate, endDate)
        );
        
        verify(clientExistencePort).existsById(clientId);
        verify(vehicleStatusPort).getStatus(vehicleId);
        verifyNoInteractions(contractRepository);
    }

    @Test
    void createContract_OverlappingContract_ThrowsOverlapException() {
        // Given
        when(clientExistencePort.existsById(clientId)).thenReturn(true);
        when(vehicleStatusPort.getStatus(vehicleId)).thenReturn(VehicleStatus.AVAILABLE);
        
        Contract existingContract = new Contract(
            UUID.randomUUID(), UUID.randomUUID(), vehicleId,
            startDate, endDate, ContractStatus.IN_PROGRESS
        );
        when(contractRepository.findOverlappingContracts(vehicleId, startDate, endDate))
            .thenReturn(List.of(existingContract));

        // When & Then
        assertThrows(OverlapException.class, () ->
            contractService.create(clientId, vehicleId, startDate, endDate)
        );
        
        verify(contractRepository).findOverlappingContracts(vehicleId, startDate, endDate);
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
        assertThrows(ContractNotFoundException.class, () ->
            contractService.start(contractId)
        );
    }

    @Test
    void startContract_AlreadyInProgress_ThrowsTransitionNotAllowedException() {
        // Given
        UUID contractId = UUID.randomUUID();
        Contract contract = new Contract(contractId, clientId, vehicleId, startDate, endDate, ContractStatus.IN_PROGRESS);
        
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // When & Then
        assertThrows(TransitionNotAllowedException.class, () ->
            contractService.start(contractId)
        );
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
        assertThrows(TransitionNotAllowedException.class, () ->
            contractService.terminate(contractId)
        );
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
        assertThrows(TransitionNotAllowedException.class, () ->
            contractService.cancel(contractId)
        );
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
        assertThrows(ContractNotFoundException.class, () ->
            contractService.findById(contractId)
        );
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
