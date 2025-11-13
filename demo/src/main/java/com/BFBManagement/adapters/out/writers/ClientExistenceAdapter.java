package com.BFBManagement.adapters.out.writers;

import com.BFBManagement.application.contrats.ports.out.ClientExistencePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implémentation stub in-memory du port ClientExistencePort.
 * Renvoie toujours true par défaut.
 * À remplacer par une vraie intégration HTTP/DB vers le service Clients.
 */
@Component
public class ClientExistenceAdapter implements ClientExistencePort {
    
    @Override
    public boolean existsById(UUID clientId) {
        // Stub : considère tous les clients comme existants
        // TODO: Remplacer par un appel REST vers le service Clients
        return true;
    }
}
