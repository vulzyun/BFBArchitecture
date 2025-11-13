package com.BFBManagement.adapters.in.rest.contrats.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour la création d'un contrat.
 */
@Schema(description = "Données requises pour créer un nouveau contrat de location")
public record CreateContratDto(
    @NotNull(message = "clientId est obligatoire")
    @Schema(
        description = "Identifiant unique du client qui loue le véhicule",
        example = "123e4567-e89b-12d3-a456-426614174000",
        required = true
    )
    UUID clientId,
    
    @NotNull(message = "vehiculeId est obligatoire")
    @Schema(
        description = "Identifiant unique du véhicule à louer",
        example = "987fcdeb-51a2-43d7-b123-987654321abc",
        required = true
    )
    UUID vehiculeId,
    
    @NotNull(message = "dateDebut est obligatoire")
    @Schema(
        description = "Date de début de la location (format ISO 8601)",
        example = "2025-11-10",
        required = true,
        type = "string",
        format = "date"
    )
    LocalDate dateDebut,
    
    @NotNull(message = "dateFin est obligatoire")
    @Schema(
        description = "Date de fin prévue de la location (format ISO 8601)",
        example = "2025-11-20",
        required = true,
        type = "string",
        format = "date"
    )
    LocalDate dateFin
) {}
