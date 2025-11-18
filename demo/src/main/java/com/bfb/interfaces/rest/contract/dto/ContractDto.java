package com.bfb.interfaces.rest.contract.dto;

import com.bfb.business.contract.model.ContractStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for contract response.
 */
@Schema(description = "Complete representation of a rental contract")
public record ContractDto(
    @Schema(
        description = "Unique contract identifier",
        example = "a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6"
    )
    UUID id,
    
    @Schema(
        description = "Client identifier",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID clientId,
    
    @Schema(
        description = "Vehicle identifier",
        example = "987fcdeb-51a2-43d7-b123-987654321abc"
    )
    UUID vehicleId,
    
    @Schema(
        description = "Rental start date",
        example = "2025-11-10",
        type = "string",
        format = "date"
    )
    LocalDate startDate,
    
    @Schema(
        description = "Rental end date",
        example = "2025-11-20",
        type = "string",
        format = "date"
    )
    LocalDate endDate,
    
    @Schema(
        description = "Current contract status",
        example = "IN_PROGRESS"
    )
    ContractStatus status
) {}
