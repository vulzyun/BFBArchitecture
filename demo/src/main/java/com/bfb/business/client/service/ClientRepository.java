package com.bfb.business.client.service;

import com.bfb.business.client.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(UUID id);
    List<Client> findAll();
    Page<Client> findAll(Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    
    boolean existsByFirstNameAndLastNameAndBirthDate(String firstName, String lastName, LocalDate birthDate);
    
    boolean existsByLicenseNumber(String licenseNumber);
    
    boolean existsByLicenseNumberAndIdNot(String licenseNumber, UUID excludeClientId);
}
