package com.bfb.business.client.service;

import com.bfb.business.client.model.Client;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Business service for client management.
 */
@Service
public class ClientService {

    public List<Client> findAll() {
        // Placeholder implementation
        return new ArrayList<>();
    }

    public boolean exists(UUID clientId) {
        // Placeholder implementation
        return true;
    }
}
