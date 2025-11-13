package com.BFBManagement.adapters.in.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI/Swagger pour la documentation de l'API.
 * Génère une documentation interactive accessible via /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bfbManagementOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("BFB Management - API Contrats")
                .description("""
                    # API REST pour la gestion des contrats de location de véhicules
                    
                    Cette API permet de gérer le cycle de vie complet des contrats de location :
                    
                    ## Fonctionnalités principales
                    
                    - **Création de contrats** avec validation automatique des règles métier
                    - **Gestion des états** : EN_ATTENTE → EN_COURS → TERMINE / EN_RETARD / ANNULE
                    - **Vérification des chevauchements** de dates pour un même véhicule
                    - **Recherche multicritère** par client, véhicule ou état
                    - **Job automatique** de marquage des contrats en retard
                    
                    ## Architecture
                    
                    L'application suit une architecture **Hexagonale** (Ports & Adapters) :
                    - **Adapters-in-rest** : Controllers REST et DTOs
                    - **Application** : Services métier et ports
                    - **Domain** : Modèles de domaine et règles métier pures
                    - **Adapters-out-bdd** : Persistence JPA
                    
                    ## Règles métier importantes
                    
                    1. Un contrat ne peut être créé que si le véhicule est disponible
                    2. Les dates de début et fin doivent être cohérentes (début < fin)
                    3. Pas de chevauchement de dates pour un même véhicule
                    4. Transitions d'état contrôlées (machine à états)
                    
                    ## Codes de réponse HTTP
                    
                    - **200 OK** : Opération réussie
                    - **201 Created** : Ressource créée avec succès
                    - **400 Bad Request** : Erreur de validation
                    - **404 Not Found** : Ressource introuvable
                    - **409 Conflict** : Conflit métier (chevauchement, véhicule indisponible)
                    - **422 Unprocessable Entity** : Transition d'état interdite
                    - **500 Internal Server Error** : Erreur serveur
                    """)
                .version("v1.0.0")
                .contact(new Contact()
                    .name("BFB Management Team")
                    .email("support@bfbmanagement.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Serveur de développement local"),
                new Server()
                    .url("https://api.bfbmanagement.com")
                    .description("Serveur de production")
            ));
    }
}
