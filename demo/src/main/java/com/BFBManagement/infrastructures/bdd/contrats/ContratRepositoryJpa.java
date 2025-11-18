package com.BFBManagement.infrastructures.bdd.contrats;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.BFBManagement.business.contrats.model.EtatContrat;

/**
 * Repository Spring Data JPA pour l'accès aux contrats en base de données.
 */
@Repository
public interface ContratRepositoryJpa extends JpaRepository<ContratJpaEntity, UUID> {

    /**
     * Trouve tous les contrats d'un véhicule ayant un état spécifique.
     */
    List<ContratJpaEntity> findByVehiculeIdAndEtat(UUID vehiculeId, EtatContrat etat);
    
    /**
     * Trouve les contrats "occupants" d'un véhicule qui chevauchent une période donnée.
     * 
     * @param vehiculeId l'ID du véhicule
     * @param dateDebut début de la période à vérifier
     * @param dateFin fin de la période à vérifier
     * @return liste des contrats en conflit potentiel
     */
    @Query("SELECT c FROM ContratJpaEntity c WHERE c.vehiculeId = :vehiculeId " +
           "AND c.etat IN ('EN_ATTENTE', 'EN_COURS', 'EN_RETARD') " +
           "AND NOT (c.dateFin < :dateDebut OR c.dateDebut > :dateFin)")
    List<ContratJpaEntity> findOverlappingContrats(
        @Param("vehiculeId") UUID vehiculeId,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin
    );

    /**
     * Trouve tous les contrats par état.
     */
    List<ContratJpaEntity> findByEtat(EtatContrat etat);

    /**
     * Trouve les contrats selon des critères de recherche optionnels.
     */
    @Query("SELECT c FROM ContratJpaEntity c WHERE " +
           "(:clientId IS NULL OR c.clientId = :clientId) AND " +
           "(:vehiculeId IS NULL OR c.vehiculeId = :vehiculeId) AND " +
           "(:etat IS NULL OR c.etat = :etat)")
    List<ContratJpaEntity> findByCriteria(
        @Param("clientId") UUID clientId,
        @Param("vehiculeId") UUID vehiculeId,
        @Param("etat") EtatContrat etat
    );
}
