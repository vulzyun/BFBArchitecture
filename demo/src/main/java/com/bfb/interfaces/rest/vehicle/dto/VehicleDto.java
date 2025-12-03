package com.bfb.interfaces.rest.vehicle.dto;

import com.bfb.business.vehicle.model.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;

import java.util.UUID;
import java.time.LocalDate;

@Schema(description = "Vehicle representation")
public record VehicleDto(
    @Schema(description = "Vehicle unique identifier")
    UUID id,
    
    @Schema(description = "Vehicle brand")
    String brand,
    
    @Schema(description = "Vehicle model")
    String model,

    @Schema(description = "Vehicle motorization")
    String motorization,

    @Schema(description = "Vehicle color")
    String color,

    @Schema(description = "Vehicle registration plate")
    String registrationPlate,

    @Schema(description = "Vehicle purchase date")
    LocalDate purchaseDate,
    
    @Schema(description = "Vehicle current status")
    VehicleStatus status
) {}
