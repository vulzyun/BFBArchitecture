package com.bfb.business.client.service;

import com.bfb.business.client.exception.ClientNotFoundException;
import com.bfb.business.client.exception.DuplicateEmailException;
import com.bfb.business.client.model.Client;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business service for client management.
 */
@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client create(String name, String email) {
        // Check for duplicate email
        if (clientRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException(
                String.format("Client with email %s already exists", email)
            );
        }
        
        Client client = new Client(null, name, email);
        return clientRepository.save(client);
    }

    public Client findById(UUID id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new ClientNotFoundException(
                String.format("Client %s not found", id)
            ));
    }

    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public boolean exists(UUID clientId) {
        return clientRepository.findById(clientId).isPresent();
    }

    public Client update(UUID id, String name, String email) {
        Client client = findById(id);
        
        // Check if email is changing and if it's already taken
        if (!client.getEmail().equals(email) && clientRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException(
                String.format("Client with email %s already exists", email)
            );
        }
        
        client.setName(name);
        client.setEmail(email);
        return clientRepository.save(client);
    }

    public void delete(UUID id) {
        if (!clientRepository.findById(id).isPresent()) {
            throw new ClientNotFoundException(
                String.format("Client %s not found", id)
            );
        }
        clientRepository.deleteById(id);
    }
}
