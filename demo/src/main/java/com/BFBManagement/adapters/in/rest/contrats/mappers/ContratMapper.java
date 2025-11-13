package com.BFBManagement.adapters.in.rest.contrats.mappers;

import com.BFBManagement.adapters.in.rest.contrats.dto.ContratDto;
import com.BFBManagement.domain.contrats.Contrat;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Entity et DTO.
 */
@Component
public class ContratMapper {
    
    public ContratDto toDto(Contrat contrat) {
        return new ContratDto(
            contrat.getId(),
            contrat.getClientId(),
            contrat.getVehiculeId(),
            contrat.getDateDebut(),
            contrat.getDateFin(),
            contrat.getEtat()
        );
    }
}
