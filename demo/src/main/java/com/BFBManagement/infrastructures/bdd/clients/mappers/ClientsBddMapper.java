package com.BFBManagement.infrastructures.bdd.clients.mappers;
import org.springframework.stereotype.Service;

import com.BFBManagement.business.clients.model.Clients;
import com.BFBManagement.infrastructures.bdd.clients.ClientsJpaEntity;

@Service
public class ClientsBddMapper {
    

    public Clients toDomain(ClientsJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Clients(
            entity.getId(),
            entity.getPrenom(),
            entity.getNom(),
            entity.getAdresse(),
            entity.getNumPermis(),
            entity.getDateNaissance()
        );
    }
    public ClientsJpaEntity toEntity(Clients domain) {
        if (domain == null) {
            return null;
        }
        ClientsJpaEntity entity = new ClientsJpaEntity();
        entity.setId(domain.getId());
        entity.setPrenom(domain.getPrenom());
        entity.setNom(domain.getNom());
        entity.setAdresse(domain.getAdresse());
        entity.setNumPermis(domain.getNumPermis());
        entity.setDateNaissance(domain.getDateNaissance());


        return entity;
    }
}
