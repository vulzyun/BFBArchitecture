package com.BFBManagement.business.contrats.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité de domaine représentant un contrat de location.
 * Pure business logic - aucune dépendance vers des frameworks.
 */
public class Contrat {

    private UUID id;
    private UUID clientId;
    private UUID vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private EtatContrat etat;

    // Constructeurs
    public Contrat() {
    }

    public Contrat(UUID id, UUID clientId, UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin, EtatContrat etat) {
        this.id = id;
        this.clientId = clientId;
        this.vehiculeId = vehiculeId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.etat = etat;
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public UUID getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(UUID vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public EtatContrat getEtat() {
        return etat;
    }

    public void setEtat(EtatContrat etat) {
        this.etat = etat;
    }
    
    // Méthodes métier pour les transitions d'état
    public void start() {
        if (!Rules.transitionAllowed(this.etat, EtatContrat.EN_COURS)) {
            throw new IllegalStateException(
                String.format("Impossible de démarrer un contrat en état %s", this.etat)
            );
        }
        this.etat = EtatContrat.EN_COURS;
    }
    
    public void terminate() {
        if (!Rules.transitionAllowed(this.etat, EtatContrat.TERMINE)) {
            throw new IllegalStateException(
                String.format("Impossible de terminer un contrat en état %s", this.etat)
            );
        }
        this.etat = EtatContrat.TERMINE;
    }
    
    public void cancel() {
        if (!Rules.transitionAllowed(this.etat, EtatContrat.ANNULE)) {
            throw new IllegalStateException(
                String.format("Impossible d'annuler un contrat en état %s", this.etat)
            );
        }
        this.etat = EtatContrat.ANNULE;
    }
    
    public void markLate() {
        if (!Rules.transitionAllowed(this.etat, EtatContrat.EN_RETARD)) {
            throw new IllegalStateException(
                String.format("Impossible de marquer en retard un contrat en état %s", this.etat)
            );
        }
        this.etat = EtatContrat.EN_RETARD;
    }
    
    /**
     * Vérifie si le contrat "occupe" le véhicule (états actifs).
     */
    public boolean isOccupant() {
        return etat == EtatContrat.EN_ATTENTE 
            || etat == EtatContrat.EN_COURS 
            || etat == EtatContrat.EN_RETARD;
    }
}
