package com.BFBManagement.presentation.contrats;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI/Swagger pour la documentation de l'API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bfbManagementOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("BFB Management - API Contrats")
                .description("API REST pour la gestion des contrats de location de véhicules")
                .version("v1.0.0")
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
