package com.bfb.infrastructure.vehicle;

import com.bfb.business.vehicle.model.VehicleStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class VehicleEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String brand;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false, length = 15)
    private String motorization;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false, length = 50)
    private String registrationPlate;

    @Column()
    private LocalDate purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    public VehicleEntity() {
    }

    public VehicleEntity(UUID id, String brand, String model, String motorization, String color, String registrationPlate, LocalDate purchaseDate, VehicleStatus status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.motorization = motorization;
        this.color = color;
        this.registrationPlate = registrationPlate;
        this.purchaseDate = purchaseDate;
        this.status = status;
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
