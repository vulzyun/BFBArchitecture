package com.bfb.infrastructure.persistence.client;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA entity for client persistence.
 */
@Entity
@Table(name = "clients", indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true)
})
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    public ClientEntity() {
    }

    public ClientEntity(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
