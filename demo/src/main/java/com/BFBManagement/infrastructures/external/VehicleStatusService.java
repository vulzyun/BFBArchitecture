package com.BFBManagement.infrastructures.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.BFBManagement.business.vehicules.model.EtatVehicule;

/**
 * Service stub pour récupérer l'état des véhicules.
 * Renvoie toujours DISPONIBLE par défaut.
 * À remplacer par une vraie intégration HTTP/DB vers le service Véhicules.
 */
@Component
public class VehicleStatusService {
    
    public EtatVehicule getStatus(UUID vehiculeId) {
        // Stub : considère tous les véhicules comme disponibles
        // TODO: Remplacer par un appel REST vers le service Véhicules
        return EtatVehicule.DISPONIBLE;
    }
}
