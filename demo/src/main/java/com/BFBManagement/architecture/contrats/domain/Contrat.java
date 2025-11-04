package com.BFBManagement.architecture.contrats.domain;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité JPA représentant un contrat de location.
 */
@Hidden
@Entity
@Table(name = "contrats", indexes = {
    @Index(name = "idx_vehicule_dates", columnList = "vehicule_id, date_debut, date_fin"),
    @Index(name = "idx_etat", columnList = "etat")
})
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "vehicule_id", nullable = false)
    private UUID vehiculeId;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtatContrat etat;

    // Constructeurs
    public Contrat() {
    }

    public Contrat(UUID clientId, UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin, EtatContrat etat) {
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
