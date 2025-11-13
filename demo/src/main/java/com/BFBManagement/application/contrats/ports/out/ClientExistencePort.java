package com.BFBManagement.application.contrats.ports.out;

import java.util.UUID;

/**
 * Port pour vérifier l'existence d'un client.
 * Permet de découpler le domaine Contrats du domaine Clients.
 */
public interface ClientExistencePort {
    /**
     * Vérifie si un client existe.
     * 
     * @param clientId identifiant du client
     * @return true si le client existe, false sinon
     */
    boolean existsById(UUID clientId);
}
