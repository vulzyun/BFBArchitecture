package com.bfb.infrastructure.persistence.client;

import java.time.LocalDate;
import java.util.UUID;

import com.bfb.infrastructure.persistence.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for client persistence.
 * Extends BaseEntity for automatic audit field management (createdAt, updatedAt).
 */
@Entity
@Table(name = "clients")
public class ClientEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "client_id")
    private UUID clientId;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 255)
    private String adresse;

    @Column(name = "num_permis", nullable = false, length = 50)
    private String numPermis;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    public ClientEntity() {
    }

    public ClientEntity(UUID clientId, String prenom, String nom, String adresse, String numPermis, LocalDate dateNaissance) {
        this.clientId = clientId;
        this.prenom = prenom;
        this.nom = nom;
        this.adresse = adresse;
        this.numPermis = numPermis;
        this.dateNaissance = dateNaissance;
    }

    public UUID getId() {
        return clientId;
    }

    public void setId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumPermis() {
        return numPermis;
    }

    public void setNumPermis(String numPermis) {
        this.numPermis = numPermis;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}
