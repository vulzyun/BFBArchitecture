package com.BFBManagement.infrastructures.bdd.contrats;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.BFBManagement.business.contrats.model.Contrat;
import com.BFBManagement.business.contrats.model.EtatContrat;

/**
 * Service d'accès aux données pour les contrats (architecture 3-couches).
 * Gère la persistance et la conversion Entity <-> Model.
 */
@Component
public class ContratBddService {

    private final ContratRepositoryJpa jpaRepository;

    public ContratBddService(ContratRepositoryJpa jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public Contrat save(Contrat contrat) {
        ContratJpaEntity entity = toEntity(contrat);
        ContratJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    public Optional<Contrat> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(this::toDomain);
    }

    public List<Contrat> findByVehiculeIdAndEtat(UUID vehiculeId, EtatContrat etat) {
        return jpaRepository.findByVehiculeIdAndEtat(vehiculeId, etat)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public List<Contrat> findOverlappingContrats(UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        return jpaRepository.findOverlappingContrats(vehiculeId, dateDebut, dateFin)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public List<Contrat> findByEtat(EtatContrat etat) {
        return jpaRepository.findByEtat(etat)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    public List<Contrat> findByCriteria(UUID clientId, UUID vehiculeId, EtatContrat etat) {
        return jpaRepository.findByCriteria(clientId, vehiculeId, etat)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    // === Mappers ===

    private ContratJpaEntity toEntity(Contrat domain) {
        if (domain == null) {
            return null;
        }
        
        return new ContratJpaEntity(
            domain.getId(),
            domain.getClientId(),
            domain.getVehiculeId(),
            domain.getDateDebut(),
            domain.getDateFin(),
            domain.getEtat()
        );
    }

    private Contrat toDomain(ContratJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new Contrat(
            entity.getId(),
            entity.getClientId(),
            entity.getVehiculeId(),
            entity.getDateDebut(),
            entity.getDateFin(),
            entity.getEtat()
        );
    }
}
