package com.bfb.business.client.service;

import com.bfb.business.client.exception.DuplicateClientException;
import com.bfb.business.client.exception.DuplicateLicenseException;
import com.bfb.business.client.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService uniqueness validation rules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService - Uniqueness Validation Tests")
class ClientServiceUniquenessTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private String firstName;
    private String lastName;
    private String address;
    private String licenseNumber;
    private LocalDate birthDate;

    @BeforeEach
    void setUp() {
        firstName = "John";
        lastName = "Doe";
        address = "123 Main St";
        licenseNumber = "DL123456";
        birthDate = LocalDate.of(1990, 5, 15);
    }

    @Test
    @DisplayName("Should create client when identity is unique")
    void testCreate_WhenIdentityIsUnique_ShouldSucceed() {
        // Arrange
        when(clientRepository.existsByFirstNameAndLastNameAndBirthDate(firstName, lastName, birthDate))
            .thenReturn(false);
        when(clientRepository.existsByLicenseNumber(licenseNumber))
            .thenReturn(false);
        when(clientRepository.save(any(Client.class)))
            .thenReturn(new Client(UUID.randomUUID(), firstName, lastName, address, licenseNumber, birthDate));

        // Act
        Client result = clientService.create(firstName, lastName, address, licenseNumber, birthDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(firstName);
        verify(clientRepository).existsByFirstNameAndLastNameAndBirthDate(firstName, lastName, birthDate);
        verify(clientRepository).existsByLicenseNumber(licenseNumber);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw DuplicateClientException when client with same identity exists")
    void testCreate_WhenClientIdentityExists_ShouldThrowException() {
        // Arrange
        when(clientRepository.existsByFirstNameAndLastNameAndBirthDate(firstName, lastName, birthDate))
            .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> 
            clientService.create(firstName, lastName, address, licenseNumber, birthDate)
        )
            .isInstanceOf(DuplicateClientException.class)
            .hasMessageContaining("John Doe")
            .hasMessageContaining("1990-05-15");

        verify(clientRepository).existsByFirstNameAndLastNameAndBirthDate(firstName, lastName, birthDate);
        verify(clientRepository, never()).existsByLicenseNumber(anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw DuplicateLicenseException when license number exists")
    void testCreate_WhenLicenseNumberExists_ShouldThrowException() {
        // Arrange
        when(clientRepository.existsByFirstNameAndLastNameAndBirthDate(firstName, lastName, birthDate))
            .thenReturn(false);
        when(clientRepository.existsByLicenseNumber(licenseNumber))
            .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> 
            clientService.create(firstName, lastName, address, licenseNumber, birthDate)
        )
            .isInstanceOf(DuplicateLicenseException.class)
            .hasMessageContaining("DL123456")
            .hasMessageContaining("already registered");

        verify(clientRepository).existsByFirstNameAndLastNameAndBirthDate(firstName, lastName, birthDate);
        verify(clientRepository).existsByLicenseNumber(licenseNumber);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should allow updating client with same license number")
    void testUpdate_WhenKeepingSameLicenseNumber_ShouldSucceed() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Client existingClient = new Client(clientId, firstName, lastName, address, licenseNumber, birthDate);
        
        when(clientRepository.findById(clientId)).thenReturn(java.util.Optional.of(existingClient));
        when(clientRepository.existsByLicenseNumberAndIdNot(licenseNumber, clientId)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        // Act
        Client result = clientService.update(clientId, firstName, lastName, "New Address", licenseNumber, birthDate);

        // Assert
        assertThat(result).isNotNull();
        verify(clientRepository).existsByLicenseNumberAndIdNot(licenseNumber, clientId);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw DuplicateLicenseException when updating to existing license number")
    void testUpdate_WhenChangingToExistingLicenseNumber_ShouldThrowException() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        String newLicenseNumber = "DL999999";
        Client existingClient = new Client(clientId, firstName, lastName, address, licenseNumber, birthDate);
        
        when(clientRepository.findById(clientId)).thenReturn(java.util.Optional.of(existingClient));
        when(clientRepository.existsByLicenseNumberAndIdNot(newLicenseNumber, clientId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> 
            clientService.update(clientId, firstName, lastName, address, newLicenseNumber, birthDate)
        )
            .isInstanceOf(DuplicateLicenseException.class)
            .hasMessageContaining("DL999999");

        verify(clientRepository).existsByLicenseNumberAndIdNot(newLicenseNumber, clientId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should validate business rule: client unique by (firstName, lastName, birthDate)")
    void testBusinessRule_ClientUniqueness() {
        // This test documents the business rule
        // Rule: "Un client doit être unique (par son nom, son prénom et sa date de naissance)"
        
        String testFirstName = "Alice";
        String testLastName = "Smith";
        LocalDate testBirthDate = LocalDate.of(1985, 3, 20);
        
        when(clientRepository.existsByFirstNameAndLastNameAndBirthDate(testFirstName, testLastName, testBirthDate))
            .thenReturn(true);

        assertThatThrownBy(() -> 
            clientService.create(testFirstName, testLastName, "Address 1", "LIC001", testBirthDate)
        )
            .isInstanceOf(DuplicateClientException.class);
    }

    @Test
    @DisplayName("Should validate business rule: license number uniqueness")
    void testBusinessRule_LicenseNumberUniqueness() {
        // This test documents the business rule
        // Rule: "Deux clients distincts ne peuvent pas avoir le même numéro de permis"
        
        when(clientRepository.existsByFirstNameAndLastNameAndBirthDate(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByLicenseNumber("SHARED-LICENSE"))
            .thenReturn(true);

        assertThatThrownBy(() -> 
            clientService.create("Bob", "Jones", "Address", "SHARED-LICENSE", LocalDate.of(1992, 1, 1))
        )
            .isInstanceOf(DuplicateLicenseException.class)
            .hasMessageContaining("SHARED-LICENSE");
    }
}
