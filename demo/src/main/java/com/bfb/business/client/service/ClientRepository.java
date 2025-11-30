package com.bfb.business.client.service;

import com.bfb.business.client.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Client persistence operations.
 * Defined in business layer, implemented in infrastructure layer.
 */
public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(UUID id);
    List<Client> findAll();
    Page<Client> findAll(Pageable pageable);
    Optional<Client> findByEmail(String email);
    void deleteById(UUID id);
}
