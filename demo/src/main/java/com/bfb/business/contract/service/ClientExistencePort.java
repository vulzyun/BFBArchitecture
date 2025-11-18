package com.bfb.business.contract.service;

import java.util.UUID;

/**
 * Port interface for client existence verification.
 * Defined in business layer, implemented in infrastructure layer.
 */
public interface ClientExistencePort {
    boolean existsById(UUID clientId);
}
