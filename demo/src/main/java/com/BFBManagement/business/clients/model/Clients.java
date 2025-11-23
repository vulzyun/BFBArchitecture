package com.BFBManagement.business.clients.model;

import java.time.LocalDate;
import java.util.UUID;

public class Clients {
    private UUID clientId; 
    private String prenom;
    private String nom;
    private String adresse;
    private String numPermis;
    private LocalDate dateNaissance;

    // Constructeurs
    public Clients(UUID clientId, String prenom, String nom,String adresse,String numPermis, LocalDate dateNaissance) {
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

}
