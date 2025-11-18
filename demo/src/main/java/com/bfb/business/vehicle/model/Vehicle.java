package com.bfb.business.vehicle.model;

import java.util.UUID;

/**
 * Domain model representing a vehicle.
 */
public class Vehicle {
    private UUID id;
    private String brand;
    private String model;
    private VehicleStatus status;

    public Vehicle() {
    }

    public Vehicle(UUID id, String brand, String model, VehicleStatus status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
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

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
