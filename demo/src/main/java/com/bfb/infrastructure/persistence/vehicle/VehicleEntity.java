package com.bfb.infrastructure.persistence.vehicle;

import com.bfb.business.vehicle.model.VehicleStatus;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    public VehicleEntity() {
    }

    public VehicleEntity(UUID id, String brand, String model, VehicleStatus status) {
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
