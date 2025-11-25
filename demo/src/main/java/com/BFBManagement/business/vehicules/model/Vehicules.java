package com.BFBManagement.business.vehicules.model;

import java.time.LocalDate;
import java.util.UUID;

public class Vehicules {
    private UUID vehicleId;
    private String marque;
    private String motorisation;
    private String modele;
    private String couleur;
    private int id_immatriculation;
    private  LocalDate date_acquisition;

    public Vehicules(String marque, String modele, String couleur, int id_immatriculation, LocalDate date_acquisition, EtatVehicule etat) {
        this.vehicleId = UUID.randomUUID();
        this.marque = marque;
        this.modele = modele;
        this.couleur = couleur;
        this.id_immatriculation = id_immatriculation;
        this.date_acquisition = date_acquisition;
    }

    public UUID getId() {
        return vehicleId;
    }
    public void setId(UUID id) {
        this.vehicleId = id;
    }

    public String getMarque() {
        return marque;
    }
    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }
    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getCouleur() {
        return couleur;
    }
    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public int getId_immatriculation() {
        return id_immatriculation;
    }
    public void setId_immatriculation(int id_immatriculation) {
        this.id_immatriculation = id_immatriculation;
    }

    public LocalDate getDate_acquisition() {
        return date_acquisition;
    }
    public void setDate_acquisition(LocalDate date_acquisition) {
        this.date_acquisition = date_acquisition;
    }
}
