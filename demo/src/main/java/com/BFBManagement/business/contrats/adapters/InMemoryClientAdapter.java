package com.BFBManagement.business.contrats.adapters;

import com.BFBManagement.business.contrats.ports.ClientExistencePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implémentation stub in-memory du port ClientExistencePort.
 * Renvoie toujours true par défaut.
 * À remplacer par une vraie intégration HTTP/DB.
 */
@Component
public class InMemoryClientAdapter implements ClientExistencePort {
    
    @Override
    public boolean existsById(UUID clientId) {
        // Stub : considère tous les clients comme existants
        return true;
    }
}
