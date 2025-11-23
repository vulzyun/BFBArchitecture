package com.BFBManagement.infrastructures.bdd.clients;

import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entité JPA représentant un client (permet à spring de faire le mapping avec la table clients en base de données).
 */

@Hidden
@Entity
@Table(name = "clients", indexes = {
    @Index(name = "client_id", columnList = "client_id, prenom, nom, date_naissance, num_permis, adresse")
})
public class ClientsJpaEntity {

    @Id
    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "num_permis", nullable = false)
    private String numPermis;

    @Column(name = "adresse", nullable = true)
    private String adresse;


    // Constructeurs
    public ClientsJpaEntity() {
    }

    public ClientsJpaEntity(UUID clientId, String prenom, String nom, LocalDate dateNaissance, String numPermis, String adresse) {
        this.clientId = clientId;
        this.prenom = prenom;
        this.nom = nom;
        this.dateNaissance = dateNaissance;
        this.numPermis = numPermis;
        this.adresse = adresse;
    }

    // Getters et Setters
    public UUID getId() {
        return clientId;
    }

    public void setId(UUID id) {
        this.clientId = clientId;
    }


    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getNumPermis(String numPermis) {
        return numPermis;
    }

    public void setNumPermis(String numPermis) {
        this.numPermis = numPermis;
    }
}
