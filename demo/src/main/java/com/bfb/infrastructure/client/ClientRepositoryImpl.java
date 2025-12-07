package com.bfb.infrastructure.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.bfb.business.client.model.Client;
import com.bfb.business.client.service.ClientRepository;

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

    @Override
    public boolean existsByFirstNameAndLastNameAndBirthDate(String firstName, String lastName, java.time.LocalDate birthDate) {
        return jpaRepository.existsByFirstNameAndLastNameAndBirthDate(firstName, lastName, birthDate);
    }

    @Override
    public boolean existsByLicenseNumber(String licenseNumber) {
        return jpaRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    public boolean existsByLicenseNumberAndIdNot(String licenseNumber, UUID excludeClientId) {
        return jpaRepository.existsByLicenseNumberAndIdNot(licenseNumber, excludeClientId);
    }

    private ClientEntity toEntity(Client client) {
        return new ClientEntity(
            client.getId(),
            client.getFirstName(),
            client.getLastName(),
            client.getAddress(),
            client.getLicenseNumber(),
            client.getBirthDate()
        );
    }

    private Client toDomain(ClientEntity entity) {
        return new Client(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getAddress(),
            entity.getLicenseNumber(),
            entity.getBirthDate()
        );
    }
}
 