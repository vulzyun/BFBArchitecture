package com.BFBManagement.presentation.contrats;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour la création d'un contrat.
 * La validation de la cohérence des dates (dateFin > dateDebut) 
 * est gérée par le service métier.
 */
public record CreateContratDto(
    @NotNull(message = "clientId est obligatoire")
    UUID clientId,
    
    @NotNull(message = "vehiculeId est obligatoire")
    UUID vehiculeId,
    
    @NotNull(message = "dateDebut est obligatoire")
    LocalDate dateDebut,
    
    @NotNull(message = "dateFin est obligatoire")
    LocalDate dateFin
) {}
