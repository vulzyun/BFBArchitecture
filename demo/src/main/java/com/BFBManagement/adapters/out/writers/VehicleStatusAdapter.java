package com.BFBManagement.adapters.out.writers;

import com.BFBManagement.application.contrats.ports.out.VehicleStatusPort;
import com.BFBManagement.domain.vehicules.EtatVehicule;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implémentation stub in-memory du port VehicleStatusPort.
 * Renvoie toujours DISPONIBLE par défaut.
 * À remplacer par une vraie intégration HTTP/DB vers le service Véhicules.
 */
@Component
public class VehicleStatusAdapter implements VehicleStatusPort {
    
    @Override
    public EtatVehicule getStatus(UUID vehiculeId) {
        // Stub : considère tous les véhicules comme disponibles
        // TODO: Remplacer par un appel REST vers le service Véhicules
        return EtatVehicule.DISPONIBLE;
    }
}
