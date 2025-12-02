package com.bfb.interfaces.rest.vehicle.dto;

import com.bfb.business.vehicle.model.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Vehicle representation")
public record VehicleDto(
    @Schema(description = "Vehicle unique identifier")
    UUID id,
    
    @Schema(description = "Vehicle brand")
    String brand,
    
    @Schema(description = "Vehicle model")
    String model,
    
    @Schema(description = "Vehicle current status")
    VehicleStatus status
) {}
