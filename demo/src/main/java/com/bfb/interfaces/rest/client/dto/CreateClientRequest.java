package com.bfb.interfaces.rest.client.dto;

import com.bfb.interfaces.rest.validation.AdultAge;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Data required to create a new client")
public record CreateClientRequest(
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    @Schema(
        description = "Client first name",
        example = "John",
        minLength = 2,
        maxLength = 100
    )
    String firstName,
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    @Schema(
        description = "Client last name",
        example = "Doe",
        minLength = 2,
        maxLength = 100
    )
    String lastName,
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(
        description = "Client address (optional)",
        example = "123 Main Street, New York, NY 10001",
        maxLength = 255
    )
    String address,
    
    @NotBlank(message = "License number is required")
    @Size(min = 5, max = 50, message = "License number must be between 5 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "License number must contain only uppercase letters, numbers, and hyphens")
    @Schema(
        description = "Driver's license number",
        example = "ABC123456",
        minLength = 5,
        maxLength = 50
    )
    String licenseNumber,
    
    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    @AdultAge(minAge = 18, message = "Driver must be at least 18 years old")
    @Schema(
        description = "Client date of birth - must be at least 18 years old",
        example = "1990-05-15",
        type = "string",
        format = "date"
    )
    LocalDate birthDate
) {}
