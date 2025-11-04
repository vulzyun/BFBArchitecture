package com.BFBManagement.presentation.contrats;

import com.BFBManagement.infrastructure.contrats.domain.EtatContrat;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de réponse pour un contrat.
 */
public record ContratDto(
    UUID id,
    UUID clientId,
    UUID vehiculeId,
    LocalDate dateDebut,
    LocalDate dateFin,
    EtatContrat etat
) {}
