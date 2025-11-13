package com.BFBManagement.adapters.out.bdd.contrats;

import com.BFBManagement.domain.contrats.EtatContrat;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité JPA représentant un contrat de location.
 * Séparée du modèle de domaine pour isolation de l'infrastructure.
 */
@Hidden
@Entity
@Table(name = "contrats", indexes = {
    @Index(name = "idx_vehicule_dates", columnList = "vehicule_id, date_debut, date_fin"),
    @Index(name = "idx_etat", columnList = "etat")
})
public class ContratJpaEntity {

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
    public ContratJpaEntity() {
    }

    public ContratJpaEntity(UUID id, UUID clientId, UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin, EtatContrat etat) {
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
}
