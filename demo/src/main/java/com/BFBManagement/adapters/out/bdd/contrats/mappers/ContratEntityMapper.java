package com.BFBManagement.adapters.out.bdd.contrats.mappers;

import com.BFBManagement.adapters.out.bdd.contrats.ContratJpaEntity;
import com.BFBManagement.domain.contrats.Contrat;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre les entités JPA et le modèle de domaine.
 */
@Component
public class ContratEntityMapper {

    /**
     * Convertit une entité de domaine en entité JPA.
     */
    public ContratJpaEntity toEntity(Contrat domain) {
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

    /**
     * Convertit une entité JPA en entité de domaine.
     */
    public Contrat toDomain(ContratJpaEntity entity) {
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
