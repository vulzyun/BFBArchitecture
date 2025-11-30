package com.bfb.interfaces.rest.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a client.
 */
@Schema(description = "Data required to create a new client")
public record CreateClientRequest(
    @NotBlank(message = "name is required")
    @Schema(description = "Client name", example = "John Doe")
    String name,
    
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Schema(description = "Client email address", example = "john.doe@example.com")
    String email
) {}
