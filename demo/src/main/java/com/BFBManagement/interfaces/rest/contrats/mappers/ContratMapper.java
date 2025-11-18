package com.BFBManagement.interfaces.rest.contrats.mappers;

import org.springframework.stereotype.Component;

import com.BFBManagement.business.contrats.model.Contrat;
import com.BFBManagement.interfaces.rest.contrats.dto.ContratDto;

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
