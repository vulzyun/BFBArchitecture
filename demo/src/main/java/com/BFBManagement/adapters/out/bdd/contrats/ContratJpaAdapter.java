package com.BFBManagement.adapters.out.bdd.contrats;

import com.BFBManagement.adapters.out.bdd.contrats.mappers.ContratEntityMapper;
import com.BFBManagement.application.contrats.ports.out.ContratRepository;
import com.BFBManagement.domain.contrats.Contrat;
import com.BFBManagement.domain.contrats.EtatContrat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur JPA implémentant le port ContratRepository.
 * Adapte le repository Spring Data JPA au port défini dans l'application.
 */
@Component
public class ContratJpaAdapter implements ContratRepository {

    private final ContratRepositoryJpa jpaRepository;
    private final ContratEntityMapper mapper;

    public ContratJpaAdapter(ContratRepositoryJpa jpaRepository, ContratEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Contrat save(Contrat contrat) {
        ContratJpaEntity entity = mapper.toEntity(contrat);
        ContratJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Contrat> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<Contrat> findByVehiculeIdAndEtat(UUID vehiculeId, EtatContrat etat) {
        return jpaRepository.findByVehiculeIdAndEtat(vehiculeId, etat)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contrat> findOverlappingContrats(UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        return jpaRepository.findOverlappingContrats(vehiculeId, dateDebut, dateFin)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contrat> findByEtat(EtatContrat etat) {
        return jpaRepository.findByEtat(etat)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contrat> findByCriteria(UUID clientId, UUID vehiculeId, EtatContrat etat) {
        return jpaRepository.findByCriteria(clientId, vehiculeId, etat)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
