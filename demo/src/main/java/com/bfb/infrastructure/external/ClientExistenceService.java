package com.bfb.infrastructure.external;

import com.bfb.business.contract.service.ClientExistencePort;
import com.bfb.business.client.service.ClientService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of ClientExistencePort using ClientService.
 * This adapter connects the contract domain to the client domain.
 */
@Component
public class ClientExistenceService implements ClientExistencePort {

    private final ClientService clientService;

    public ClientExistenceService(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public boolean existsById(UUID clientId) {
        return clientService.exists(clientId);
    }
}
