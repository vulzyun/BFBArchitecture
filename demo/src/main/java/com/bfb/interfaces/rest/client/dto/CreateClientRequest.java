package com.bfb.interfaces.rest.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a client.
 * Includes comprehensive validation for name and email.
 */
@Schema(description = "Data required to create a new client")
public record CreateClientRequest(
    @NotBlank(message = "Name is required and cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(
        description = "Client name (2-100 characters)",
        example = "John Doe",
        minLength = 2,
        maxLength = 100
    )
    String name,
    
    @NotBlank(message = "Email is required and cannot be blank")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(
        description = "Client email address (valid format, max 100 characters)",
        example = "john.doe@example.com",
        format = "email",
        maxLength = 100
    )
    String email
) {}
