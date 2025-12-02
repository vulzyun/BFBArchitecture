package com.bfb.infrastructure.persistence.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Client.
 */
public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
    // Standard CRUD operations from JpaRepository
}
