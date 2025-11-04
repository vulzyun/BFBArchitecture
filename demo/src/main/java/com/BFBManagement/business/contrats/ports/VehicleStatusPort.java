package com.BFBManagement.business.contrats.ports;

import com.BFBManagement.business.vehicules.EtatVehicule;
import java.util.UUID;

/**
 * Port pour récupérer l'état d'un véhicule.
 * Permet de découpler le domaine Contrats du domaine Véhicules.
 */
public interface VehicleStatusPort {
    /**
     * Récupère l'état actuel d'un véhicule.
     * 
     * @param vehiculeId identifiant du véhicule
     * @return état du véhicule
     */
    EtatVehicule getStatus(UUID vehiculeId);
}
