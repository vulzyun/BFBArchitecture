package com.BFBManagement.application.contrats.ports.in;

import com.BFBManagement.domain.contrats.Contrat;
import com.BFBManagement.domain.contrats.EtatContrat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Port d'entrée (inbound) pour les cas d'usage des contrats.
 * Définit les opérations métier exposées par l'application.
 */
public interface ContratUseCase {

    /**
     * Crée un nouveau contrat avec toutes les validations métier.
     */
    Contrat create(UUID clientId, UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin);

    /**
     * Démarre un contrat (EN_ATTENTE → EN_COURS).
     */
    Contrat start(UUID contratId);

    /**
     * Termine un contrat (EN_COURS ou EN_RETARD → TERMINE).
     */
    Contrat terminate(UUID contratId);

    /**
     * Annule un contrat (EN_ATTENTE → ANNULE).
     */
    Contrat cancel(UUID contratId);

    /**
     * Marque automatiquement en retard tous les contrats EN_COURS dont la dateFin est dépassée.
     */
    int markLateIfOverdue();

    /**
     * Annule tous les contrats EN_ATTENTE pour un véhicule donné.
     */
    int cancelPendingContractsForVehicle(UUID vehiculeId);

    /**
     * Récupère un contrat par ID.
     */
    Contrat findById(UUID id);

    /**
     * Recherche des contrats selon des critères optionnels.
     */
    List<Contrat> findByCriteria(UUID clientId, UUID vehiculeId, EtatContrat etat);
}
