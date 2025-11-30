package com.bfb.infrastructure.persistence.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Base entity class providing audit fields for all entities.
 * Automatically tracks creation and modification timestamps.
 * 
 * This is a technical concern handled at the infrastructure layer,
 * keeping the domain layer clean of persistence details.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets creation timestamp before entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Updates modification timestamp before entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
