package com.BFBManagement.infrastructures.bdd.clients;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.BFBManagement.business.clients.model.Clients;
import com.BFBManagement.infrastructures.bdd.clients.mappers.ClientsBddMapper;


@Component
public class ClientsBddService {
    private final ClientsRepositoryJpa jpaRepository;
    private final ClientsBddMapper mapper; 
        
    public ClientsBddService(ClientsRepositoryJpa jpaRepository, ClientsBddMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }



    public Clients save(Clients client) {
        ClientsJpaEntity entity = mapper.toEntity(client);
        ClientsJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
        
    }

    public Clients findById(java.util.UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain)
            .orElse(null);
    }

    public Collection<Clients> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(mapper::toDomain) 
            .collect(Collectors.toList());
    }

    public Clients update(Clients client) {
        ClientsJpaEntity entity = mapper.toEntity(client);
        ClientsJpaEntity updated = jpaRepository.save(entity);
        return mapper.toDomain(updated);
    }

    public void deleteById(java.util.UUID id) {
        jpaRepository.deleteById(id);
    }
    

        
}
