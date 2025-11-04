package com.BFBManagement.presentation.contrats;

import com.BFBManagement.infrastructure.contrats.domain.EtatContrat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de réponse pour un contrat.
 */
@Schema(description = "Représentation complète d'un contrat de location")
public record ContratDto(
    @Schema(
        description = "Identifiant unique du contrat",
        example = "a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6"
    )
    UUID id,
    
    @Schema(
        description = "Identifiant du client locataire",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID clientId,
    
    @Schema(
        description = "Identifiant du véhicule loué",
        example = "987fcdeb-51a2-43d7-b123-987654321abc"
    )
    UUID vehiculeId,
    
    @Schema(
        description = "Date de début de la location",
        example = "2025-11-10",
        type = "string",
        format = "date"
    )
    LocalDate dateDebut,
    
    @Schema(
        description = "Date de fin prévue de la location",
        example = "2025-11-20",
        type = "string",
        format = "date"
    )
    LocalDate dateFin,
    
    @Schema(
        description = "État actuel du contrat",
        example = "EN_COURS",
        allowableValues = {"EN_ATTENTE", "EN_COURS", "TERMINE", "EN_RETARD", "ANNULE"}
    )
    EtatContrat etat
) {}
