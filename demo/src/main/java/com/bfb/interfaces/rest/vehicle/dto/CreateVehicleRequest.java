package com.bfb.interfaces.rest.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a vehicle.
 */
@Schema(description = "Data required to create a new vehicle")
public record CreateVehicleRequest(
    @NotBlank(message = "brand is required")
    @Schema(description = "Vehicle brand", example = "Toyota")
    String brand,
    
    @NotBlank(message = "model is required")
    @Schema(description = "Vehicle model", example = "Corolla")
    String model
) {}
