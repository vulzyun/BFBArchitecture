package com.bfb.interfaces.rest.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Data required to create a new client")
public record CreateClientRequest(
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Schema(
        description = "Client first name",
        example = "John",
        minLength = 2,
        maxLength = 100
    )
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Schema(
        description = "Client last name",
        example = "Doe",
        minLength = 2,
        maxLength = 100
    )
    String lastName,
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(
        description = "Client address",
        example = "123 Main Street, New York, NY 10001",
        maxLength = 255
    )
    String address,
    
    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    @Schema(
        description = "Driver's license number",
        example = "ABC123456",
        maxLength = 50
    )
    String licenseNumber,
    
    @NotNull(message = "Birth date is required")
    @Schema(
        description = "Client date of birth",
        example = "1990-05-15",
        type = "string",
        format = "date"
    )
    LocalDate birthDate
) {}
