package com.bfb.infrastructure.external;

import com.bfb.business.contract.service.ClientExistencePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of ClientExistencePort for external client service.
 */
@Component
public class ClientExistenceService implements ClientExistencePort {

    @Override
    public boolean existsById(UUID clientId) {
        // Placeholder: simulate external service call
        // In production, this would call an actual client service
        return true;
    }
}
