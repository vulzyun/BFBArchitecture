package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.VehicleUnavailableException;
import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.vehicle.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleAvailabilityValidator.
 */
@ExtendWith(MockitoExtension.class)
class VehicleAvailabilityValidatorTest {

    @Mock
    private VehicleService vehicleService;

    private VehicleAvailabilityValidator validator;
    private UUID clientId;
    private UUID vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        validator = new VehicleAvailabilityValidator(vehicleService);
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(7);
    }

    @Test
    void validate_VehicleAvailable_NoException() {
        // Given
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.AVAILABLE);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
        verify(vehicleService).getStatus(vehicleId);
    }

    @Test
    void validate_VehicleBroken_ThrowsVehicleUnavailableException() {
        // Given
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.BROKEN);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        VehicleUnavailableException exception = assertThrows(VehicleUnavailableException.class, 
            () -> validator.validate(context));
        
        assertTrue(exception.getMessage().contains(vehicleId.toString()));
        assertTrue(exception.getMessage().toLowerCase().contains("broken"));
        verify(vehicleService).getStatus(vehicleId);
    }

    @Test
    void validate_VehicleRented_NoException() {
        // Given - RENTED status is allowed (overlap validator handles conflicts)
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.RENTED);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
        verify(vehicleService).getStatus(vehicleId);
    }

    @Test
    void validate_ChecksStatusEachTime() {
        // Given
        when(vehicleService.getStatus(vehicleId)).thenReturn(VehicleStatus.AVAILABLE);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When
        validator.validate(context);
        validator.validate(context);

        // Then - status is checked each time
        verify(vehicleService, times(2)).getStatus(vehicleId);
    }
}
