package com.bfb.business.vehicle.model;

import java.util.UUID;
import java.time.LocalDate;

public class Vehicle {
    private UUID id;
    private String brand;
    private String model;
    private String motorization;
    private String color;
    private String registrationPlate;
    private LocalDate purchaseDate;
    private VehicleStatus status;

    public Vehicle() {
    }

    public Vehicle(UUID id, String brand, String model, String motorization, String color, String registrationPlate, LocalDate purchaseDate, VehicleStatus status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.status = status;
        this.motorization = motorization;
        this.color = color;
        this.registrationPlate = registrationPlate;
        this.purchaseDate = purchaseDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMotorization() {return motorization;}

    public void setMotorization(String motorization) {this.motorization = motorization;}

    public String getColor() {return color;}

    public void setColor(String color) {this.color = color;}

    public String getRegistrationPlate() {return registrationPlate;}

    public void setRegistrationPlate(String registrationPlate) {this.registrationPlate = registrationPlate;}

    public LocalDate getPurchaseDate() {return purchaseDate;}

    public void setPurchaseDate(LocalDate purchaseDate) {this.purchaseDate = purchaseDate;}

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
