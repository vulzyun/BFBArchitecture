package com.bfb.infrastructure.persistence.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
    boolean existsByFirstNameAndLastNameAndBirthDate(String firstName, String lastName, LocalDate birthDate);
    
    boolean existsByLicenseNumber(String licenseNumber);
    
    boolean existsByLicenseNumberAndIdNot(String licenseNumber, UUID excludeClientId);
}
