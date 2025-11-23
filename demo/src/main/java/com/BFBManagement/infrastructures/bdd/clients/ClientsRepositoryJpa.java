package com.BFBManagement.infrastructures.bdd.clients;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientsRepositoryJpa  extends JpaRepository<ClientsJpaEntity, UUID> {
    
    /*
    JpaRepository fournit déjà de nombreuses méthodes prêtes, soit
    findById, save, delete, findAll.

    */
    
}
