package com.bfb.interfaces.rest.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * DTO for client response.
 */
@Schema(description = "Client representation")
public record ClientDto(
    @Schema(description = "Client unique identifier")
    UUID id,
    
    @Schema(description = "Client name")
    String name,
    
    @Schema(description = "Client email")
    String email
) {}
