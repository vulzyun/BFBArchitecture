package com.bfb.interfaces.rest.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Client representation")
public record ClientDto(
    @Schema(description = "Client unique identifier")
    UUID id,
    
    @Schema(description = "Client first name")
    String firstName,
    
    @Schema(description = "Client last name")
    String lastName,
    
    @Schema(description = "Client address")
    String address,
    
    @Schema(description = "Driver's license number")
    String licenseNumber,
    
    @Schema(description = "Client date of birth")
    LocalDate birthDate
) {}
