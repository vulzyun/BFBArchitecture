package com.bfb.infrastructure.persistence.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for Client.
 */
public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
    // No custom methods needed - using standard CRUD from JpaRepository
}
