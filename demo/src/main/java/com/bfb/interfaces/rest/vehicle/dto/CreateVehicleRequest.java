package com.bfb.interfaces.rest.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
        example = "Toyota",
        minLength = 2,
        maxLength = 50
    )
    String brand,
    
    @NotBlank(message = "Model is required and cannot be blank")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    @Schema(
        description = "Vehicle model (1-50 characters)",
        example = "Corolla",
        minLength = 1,
        maxLength = 50
    )
    String model
) {}
