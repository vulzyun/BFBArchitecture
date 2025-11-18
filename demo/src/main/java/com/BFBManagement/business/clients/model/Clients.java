package com.BFBManagement.business.clients.model;

import java.time.LocalDate;
import java.util.UUID;

public class Clients {
    private UUID id; 
    private String nom;
    private String prenom;
    private String adresse;
    private String numPermis;
    private LocalDate dateNaissance;

    // Constructeurs
    public Clients(UUID id, String nom, String prenom, String adresse, String numPermis, LocalDate dateNaissance) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.numPermis = numPermis;
        this.dateNaissance = dateNaissance;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public static Clients create(String nom, String prenom, String adresse, String numPermis, LocalDate dateNaissance) {
        return new Clients(UUID.randomUUID(), nom, prenom, adresse, numPermis, dateNaissance);
    }
}
