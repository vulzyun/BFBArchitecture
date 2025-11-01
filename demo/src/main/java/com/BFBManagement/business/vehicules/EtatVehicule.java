package com.BFBManagement.business.vehicules;

/**
 * États possibles d'un véhicule (défini dans le domaine Véhicules).
 * Énumération minimale pour le découplage via port.
 */
public enum EtatVehicule {
    DISPONIBLE,
    EN_PANNE,
    EN_LOCATION,
    EN_MAINTENANCE
}
