package com.bfb.interfaces.rest.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for creating a vehicle.
 * Includes comprehensive validation for brand and model.
 */
@Schema(description = "Data required to create a new vehicle")
public record CreateVehicleRequest(
    @NotBlank(message = "Brand is required and cannot be blank")
    @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
    @Schema(
        description = "Vehicle brand/manufacturer (2-50 characters)",
        example = "Ford",
        minLength = 2,
        maxLength = 50
    )
    String brand,
    
    @NotBlank(message = "Model is required and cannot be blank")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    @Schema(
        description = "Vehicle model (1-50 characters)",
        example = "Explorer",
        minLength = 1,
        maxLength = 50
    )
    String model,

    @NotBlank(message = "Motorization is required and cannot be blank")
    @Size(min = 1, max = 15, message = "Model must be between 1 and 15 characters")
    @Schema(
            description = "Vehicle motorization (1-15 characters)",
            example = "Diesel",
            minLength = 1,
            maxLength = 15
    )
    String motorization,

    @NotBlank(message = "Color is required and cannot be blank")
    @Size(min = 1, max = 50, message = "Color must be between 1 and 50 characters")
    @Schema(
            description = "Vehicle color (1-50 characters)",
            example = "Red",
            minLength = 1,
            maxLength = 50
    )
    String color,

    @NotBlank(message = "Registration plate is required and cannot be blank")
    @Size(min = 1, max = 50, message = "Registration plate must be between 1 and 50 characters")
    @Schema(
            description = "Vehicle registration plate (1-50 characters)",
            example = "HL-456-GK",
            minLength = 1,
            maxLength = 50
    )
    String registrationPlate,

    @NotNull(message = "Purchase date is required")
    @Past(message = "Purchase date must be in the past")
    @Schema(
            description = "Client date of birth - must be at least 18 years old",
            example = "2025-01-01",
            type = "string",
            format = "date"
    )
    LocalDate purchaseDate

) {}
