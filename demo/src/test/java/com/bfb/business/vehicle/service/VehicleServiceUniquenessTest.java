package com.bfb.business.vehicle.service;

import com.bfb.business.vehicle.exception.DuplicateVehicleException;
import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleService uniqueness validation rules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService - Uniqueness Validation Tests")
class VehicleServiceUniquenessTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private String brand;
    private String model;
    private String motorization;
    private String color;
    private String registrationPlate;
    private LocalDate purchaseDate;

    @BeforeEach
    void setUp() {
        brand = "Ford";
        model = "Explorer";
        motorization = "Diesel";
        color = "Blue";
        registrationPlate = "AB-123-CD";
        purchaseDate = LocalDate.of(2023, 1, 15);
    }

    @Test
    @DisplayName("Should create vehicle when registration plate is unique")
    void testCreate_WhenRegistrationPlateIsUnique_ShouldSucceed() {
        // Arrange
        when(vehicleRepository.existsByRegistrationPlate(registrationPlate))
            .thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class)))
            .thenReturn(new Vehicle(UUID.randomUUID(), brand, model, motorization, color, 
                registrationPlate, purchaseDate, VehicleStatus.AVAILABLE));

        // Act
        Vehicle result = vehicleService.create(brand, model, motorization, color, registrationPlate, purchaseDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRegistrationPlate()).isEqualTo(registrationPlate);
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        verify(vehicleRepository).existsByRegistrationPlate(registrationPlate);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw DuplicateVehicleException when registration plate exists")
    void testCreate_WhenRegistrationPlateExists_ShouldThrowException() {
        // Arrange
        when(vehicleRepository.existsByRegistrationPlate(registrationPlate))
            .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> 
            vehicleService.create(brand, model, motorization, color, registrationPlate, purchaseDate)
        )
            .isInstanceOf(DuplicateVehicleException.class)
            .hasMessageContaining("AB-123-CD")
            .hasMessageContaining("already exists");

        verify(vehicleRepository).existsByRegistrationPlate(registrationPlate);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should reject duplicate registration plates with different vehicles")
    void testCreate_WhenTryingToRegisterDuplicatePlate_ShouldThrowException() {
        // Arrange - Simulating that registration plate is already taken
        String duplicatePlate = "XY-789-ZZ";
        when(vehicleRepository.existsByRegistrationPlate(duplicatePlate))
            .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> 
            vehicleService.create("Toyota", "Camry", "Hybrid", "Red", duplicatePlate, LocalDate.now())
        )
            .isInstanceOf(DuplicateVehicleException.class);

        verify(vehicleRepository).existsByRegistrationPlate(duplicatePlate);
    }

    @Test
    @DisplayName("Should allow creating multiple vehicles with different registration plates")
    void testCreate_WhenMultipleVehiclesHaveUniquePlates_ShouldSucceed() {
        // Arrange
        String plate1 = "AA-111-AA";
        String plate2 = "BB-222-BB";
        
        when(vehicleRepository.existsByRegistrationPlate(plate1)).thenReturn(false);
        when(vehicleRepository.existsByRegistrationPlate(plate2)).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Vehicle vehicle1 = vehicleService.create("Honda", "Civic", "Petrol", "White", plate1, purchaseDate);
        Vehicle vehicle2 = vehicleService.create("Mazda", "CX-5", "Diesel", "Black", plate2, purchaseDate);

        // Assert
        assertThat(vehicle1.getRegistrationPlate()).isEqualTo(plate1);
        assertThat(vehicle2.getRegistrationPlate()).isEqualTo(plate2);
        verify(vehicleRepository).existsByRegistrationPlate(plate1);
        verify(vehicleRepository).existsByRegistrationPlate(plate2);
        verify(vehicleRepository, times(2)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should validate business rule: vehicle unique by registration plate")
    void testBusinessRule_RegistrationPlateUniqueness() {
        // This test documents the business rule
        // Rule: "Un véhicule doit être unique (par leur numéro d'immatriculation)"
        
        String testPlate = "TEST-PLATE-123";
        
        when(vehicleRepository.existsByRegistrationPlate(testPlate))
            .thenReturn(true);

        assertThatThrownBy(() -> 
            vehicleService.create("Brand", "Model", "Motor", "Color", testPlate, LocalDate.now())
        )
            .isInstanceOf(DuplicateVehicleException.class)
            .hasMessageContaining(testPlate);
    }

    @Test
    @DisplayName("Should prevent registration plate conflicts across different vehicle types")
    void testBusinessRule_PlateUniquenessAcrossAllVehicleTypes() {
        // Even if brand/model/color differ, same plate should be rejected
        String sharedPlate = "CONFLICT-PLATE";
        
        when(vehicleRepository.existsByRegistrationPlate(sharedPlate))
            .thenReturn(false)  // First vehicle succeeds
            .thenReturn(true);  // Second vehicle fails

        when(vehicleRepository.save(any(Vehicle.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // First vehicle - should succeed
        Vehicle first = vehicleService.create("Audi", "A4", "Diesel", "Silver", sharedPlate, purchaseDate);
        assertThat(first).isNotNull();

        // Second vehicle with same plate - should fail
        assertThatThrownBy(() -> 
            vehicleService.create("BMW", "X5", "Petrol", "Blue", sharedPlate, purchaseDate)
        )
            .isInstanceOf(DuplicateVehicleException.class);
    }
}
