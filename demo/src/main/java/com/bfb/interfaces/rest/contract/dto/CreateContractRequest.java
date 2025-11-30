package com.bfb.interfaces.rest.contract.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a contract.
 */
@Schema(description = "Data required to create a new rental contract")
public record CreateContractRequest(
    @NotNull(message = "clientId is required")
    @Schema(
        description = "Client unique identifier",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID clientId,
    
    @NotNull(message = "vehicleId is required")
    @Schema(
        description = "Vehicle unique identifier",
        example = "987fcdeb-51a2-43d7-b123-987654321abc"
    )
    UUID vehicleId,
    
    @NotNull(message = "startDate is required")
    @Schema(
        description = "Rental start date (ISO 8601 format)",
        example = "2025-11-10",
        type = "string",
        format = "date"
    )
    LocalDate startDate,
    
    @NotNull(message = "endDate is required")
    @Schema(
        description = "Rental end date (ISO 8601 format)",
        example = "2025-11-20",
        type = "string",
        format = "date"
    )
    LocalDate endDate
) {}
