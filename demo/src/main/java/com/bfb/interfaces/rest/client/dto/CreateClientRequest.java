package com.bfb.interfaces.rest.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for creating a client.
 * Includes comprehensive validation for all required fields.
 */
@Schema(description = "Data required to create a new client")
public record CreateClientRequest(
    @NotBlank(message = "Prenom is required and cannot be blank")
    @Size(min = 2, max = 100, message = "Prenom must be between 2 and 100 characters")
    @Schema(
        description = "Client first name (2-100 characters)",
        example = "Jean",
        minLength = 2,
        maxLength = 100
    )
    String prenom,
    
    @NotBlank(message = "Nom is required and cannot be blank")
    @Size(min = 2, max = 100, message = "Nom must be between 2 and 100 characters")
    @Schema(
        description = "Client last name (2-100 characters)",
        example = "Dupont",
        minLength = 2,
        maxLength = 100
    )
    String nom,
    
    @Size(max = 255, message = "Adresse must not exceed 255 characters")
    @Schema(
        description = "Client address (max 255 characters)",
        example = "123 rue de Paris, 75001 Paris",
        maxLength = 255
    )
    String adresse,
    
    @NotBlank(message = "Num permis is required and cannot be blank")
    @Size(max = 50, message = "Num permis must not exceed 50 characters")
    @Schema(
        description = "Driver's license number (max 50 characters)",
        example = "ABC123456",
        maxLength = 50
    )
    String numPermis,
    
    @NotNull(message = "Date de naissance is required")
    @Schema(
        description = "Client date of birth",
        example = "1990-05-15",
        type = "string",
        format = "date"
    )
    LocalDate dateNaissance
) {}
