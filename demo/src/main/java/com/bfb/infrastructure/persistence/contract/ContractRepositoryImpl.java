package com.bfb.infrastructure.persistence.contract;

import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.contract.service.ContractRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ContractRepository using JPA.
 * Handles mapping between domain model and JPA entity.
 */
@Component
public class ContractRepositoryImpl implements ContractRepository {

    private final ContractJpaRepository jpaRepository;

    public ContractRepositoryImpl(ContractJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Contract save(Contract contract) {
        ContractEntity entity = toEntity(contract);
        ContractEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Contract> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Contract> findByCriteria(UUID clientId, UUID vehicleId, ContractStatus status) {
        return jpaRepository.findByCriteria(clientId, vehicleId, status)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findOverlappingContracts(UUID vehicleId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findOverlappingContracts(vehicleId, startDate, endDate)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByStatus(ContractStatus status) {
        return jpaRepository.findByStatus(status)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByVehicleIdAndStatus(UUID vehicleId, ContractStatus status) {
        return jpaRepository.findByVehicleIdAndStatus(vehicleId, status)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    // Mapping methods
    private ContractEntity toEntity(Contract contract) {
        return new ContractEntity(
            contract.getId(),
            contract.getClientId(),
            contract.getVehicleId(),
            contract.getStartDate(),
            contract.getEndDate(),
            contract.getStatus()
        );
    }

    private Contract toDomain(ContractEntity entity) {
        return new Contract(
            entity.getId(),
            entity.getClientId(),
            entity.getVehicleId(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getStatus()
        );
    }
}
