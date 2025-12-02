package com.bfb.infrastructure.persistence.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.bfb.business.client.model.Client;
import com.bfb.business.client.service.ClientRepository;

/**
 * Implementation of ClientRepository using JPA.
 */
@Component
public class ClientRepositoryImpl implements ClientRepository {

    private final ClientJpaRepository jpaRepository;

    public ClientRepositoryImpl(ClientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = toEntity(client);
        ClientEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Client> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Client> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
            .map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    // Mapping methods
    private ClientEntity toEntity(Client client) {
        return new ClientEntity(
            client.getId(),
            client.getPrenom(),
            client.getNom(),
            client.getAdresse(),
            client.getNumPermis(),
            client.getDateNaissance()
        );
    }

    private Client toDomain(ClientEntity entity) {
        return new Client(
            entity.getId(),
            entity.getPrenom(),
            entity.getNom(),
            entity.getAdresse(),
            entity.getNumPermis(),
            entity.getDateNaissance()
        );
    }
}
 