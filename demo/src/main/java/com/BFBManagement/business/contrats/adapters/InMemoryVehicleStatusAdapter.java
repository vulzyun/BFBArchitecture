package com.BFBManagement.business.contrats.adapters;

import com.BFBManagement.business.contrats.ports.VehicleStatusPort;
import com.BFBManagement.business.vehicules.EtatVehicule;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implémentation stub in-memory du port VehicleStatusPort.
 * Renvoie toujours DISPONIBLE par défaut.
 * À remplacer par une vraie intégration HTTP/DB.
 */
@Component
public class InMemoryVehicleStatusAdapter implements VehicleStatusPort {
    
    @Override
    public EtatVehicule getStatus(UUID vehiculeId) {
        // Stub : considère tous les véhicules comme disponibles
        return EtatVehicule.DISPONIBLE;
    }
}
