package com.BFBManagement.application.contrats.ports.out;

import com.BFBManagement.domain.contrats.Contrat;
import com.BFBManagement.domain.contrats.EtatContrat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port pour l'accès aux contrats en persistence.
 */
public interface ContratRepository {

    /**
     * Sauvegarde un contrat.
     */
    Contrat save(Contrat contrat);

    /**
     * Trouve un contrat par son ID.
     */
    Optional<Contrat> findById(UUID id);

    /**
     * Trouve tous les contrats d'un véhicule ayant un état spécifique.
     */
    List<Contrat> findByVehiculeIdAndEtat(UUID vehiculeId, EtatContrat etat);
    
    /**
     * Trouve les contrats "occupants" d'un véhicule qui chevauchent une période donnée.
     * 
     * @param vehiculeId l'ID du véhicule
     * @param dateDebut début de la période à vérifier
     * @param dateFin fin de la période à vérifier
     * @return liste des contrats en conflit potentiel
     */
    List<Contrat> findOverlappingContrats(UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin);

    /**
     * Trouve tous les contrats par état.
     */
    List<Contrat> findByEtat(EtatContrat etat);

    /**
     * Trouve les contrats selon des critères de recherche optionnels.
     */
    List<Contrat> findByCriteria(UUID clientId, UUID vehiculeId, EtatContrat etat);
}
