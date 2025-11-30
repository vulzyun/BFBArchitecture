package com.bfb.business.client.service;

import com.bfb.business.client.exception.ClientNotFoundException;
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

    public Client create(String prenom, String nom, String adresse, String numPermis, java.time.LocalDate dateNaissance) {
        Client client = new Client(null, prenom, nom, adresse, numPermis, dateNaissance);
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

    public org.springframework.data.domain.Page<Client> findAll(org.springframework.data.domain.Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    public boolean exists(UUID id) {
        return clientRepository.findById(id).isPresent();
    }

    public Client update(UUID id, String prenom, String nom, String adresse, String numPermis, java.time.LocalDate dateNaissance) {
        Client client = findById(id);
        
        client.setPrenom(prenom);
        client.setNom(nom);
        client.setAdresse(adresse);
        client.setNumPermis(numPermis);
        client.setDateNaissance(dateNaissance);
        return clientRepository.save(client);
    }

    public void delete(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(
                String.format("Client %s not found", id)
            );
        }
        clientRepository.deleteById(id);
    }
}
