package com.bfb.business.contract.validation;

import com.bfb.business.client.service.ClientService;
import com.bfb.business.contract.exception.ClientUnknownException;
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
 * Unit tests for ClientExistenceValidator.
 */
@ExtendWith(MockitoExtension.class)
class ClientExistenceValidatorTest {

    @Mock
    private ClientService clientService;

    private ClientExistenceValidator validator;
    private UUID clientId;
    private UUID vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        validator = new ClientExistenceValidator(clientService);
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(7);
    }

    @Test
    void validate_ClientExists_NoException() {
        // Given
        when(clientService.exists(clientId)).thenReturn(true);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(context));
        verify(clientService).exists(clientId);
    }

    @Test
    void validate_ClientDoesNotExist_ThrowsClientUnknownException() {
        // Given
        when(clientService.exists(clientId)).thenReturn(false);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When & Then
        ClientUnknownException exception = assertThrows(ClientUnknownException.class, 
            () -> validator.validate(context));
        
        assertTrue(exception.getMessage().contains(clientId.toString()));
        assertTrue(exception.getMessage().toLowerCase().contains("not found"));
        verify(clientService).exists(clientId);
    }

    @Test
    void validate_MultipleCallsSameClient_ChecksEachTime() {
        // Given
        when(clientService.exists(clientId)).thenReturn(true);
        ContractCreationContext context = new ContractCreationContext(clientId, vehicleId, startDate, endDate);

        // When
        validator.validate(context);
        validator.validate(context);

        // Then
        verify(clientService, times(2)).exists(clientId);
    }
}
