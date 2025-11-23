package com.BFBManagement.infrastructures.bdd.clients;

import org.springframework.stereotype.Component;


@Component
public class ClientsBddService {
    private final ClientsRepositoryJpa jpaRepository;
    public ClientsBddService(ClientsRepositoryJpa jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /*

    public Clients save(Clients client) {
        ClientsJpaEntity entity = ClientsBddMapper.toEntity(client);
        ClientsJpaEntity saved = jpaRepository.save(entity);
        return ClientsBddMapper.toDomain(saved);
    }
        resoudre probleme import mapper
         */
        
}
