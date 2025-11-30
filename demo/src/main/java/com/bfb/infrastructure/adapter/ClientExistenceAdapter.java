package com.bfb.infrastructure.adapter;

import com.bfb.business.contract.service.ClientExistencePort;
import com.bfb.business.client.service.ClientService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter implementing ClientExistencePort using ClientService.
 * This adapter connects the contract domain to the client domain.
 * Part of the infrastructure layer in a multi-layered architecture.
 */
@Component
public class ClientExistenceAdapter implements ClientExistencePort {

    private final ClientService clientService;

    public ClientExistenceAdapter(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public boolean existsById(UUID clientId) {
        return clientService.exists(clientId);
    }
}
