package com.BFBManagement.infrastructures.external;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Service stub pour vérifier l'existence des clients.
 * Renvoie toujours true par défaut.
 * À remplacer par une vraie intégration HTTP/DB vers le service Clients.
 */
@Component
public class ClientExistenceService {
    
    public boolean existsById(UUID clientId) {
        // Stub : considère tous les clients comme existants
        // TODO: Remplacer par un appel REST vers le service Clients
        return true;
    }
}
