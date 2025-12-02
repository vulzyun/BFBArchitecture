package com.bfb.interfaces.rest.contract.dto;

import com.bfb.interfaces.rest.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating a contract.
 * Includes comprehensive validation for business rules.
 */
@Schema(description = "Data required to create a new rental contract")
@ValidDateRange(message = "Start date must be before end date")
public record CreateContractRequest(
    @NotNull(message = "Client ID is required")
    @Schema(
        description = "Client unique identifier",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID clientId,
    
    @NotNull(message = "Vehicle ID is required")
    @Schema(
        description = "Vehicle unique identifier",
        example = "987fcdeb-51a2-43d7-b123-987654321abc"
    )
    UUID vehicleId,
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    @Schema(
        description = "Rental start date (ISO 8601 format) - must be today or in the future",
        example = "2025-12-10",
        type = "string",
        format = "date"
    )
    LocalDate startDate,
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @Schema(
        description = "Rental end date (ISO 8601 format) - must be after start date",
        example = "2025-12-20",
        type = "string",
        format = "date"
    )
    LocalDate endDate
) {
    /**
     * Note: Date coherence validation (startDate < endDate) is performed 
     * in the service layer to provide better error messages and avoid 
     * breaking Spring's validation chain.
     */
}
