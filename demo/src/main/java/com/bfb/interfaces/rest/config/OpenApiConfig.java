package com.bfb.interfaces.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Generates interactive documentation accessible via /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bfbManagementOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("BFB Management - Contracts API")
                .description("""
                    # REST API for vehicle rental contract management
                    
                    This API manages the complete lifecycle of rental contracts.
                    
                    ## Main Features
                    
                    - **Contract creation** with automatic business rule validation
                    - **Status management**: PENDING → IN_PROGRESS → COMPLETED / LATE / CANCELLED
                    - **Date overlap verification** for the same vehicle
                    - **Multi-criteria search** by client, vehicle or status
                    - **Automatic job** for marking late contracts
                    
                    ## Architecture
                    
                    The application follows a **Clean 3-Layer Architecture**:
                    - **Interface Layer**: REST Controllers and DTOs
                    - **Business Layer**: Services and domain models
                    - **Infrastructure Layer**: JPA persistence and external services
                    
                    ## Important Business Rules
                    
                    1. A contract can only be created if the vehicle is available
                    2. Start and end dates must be coherent (start < end)
                    3. No date overlap for the same vehicle
                    4. Controlled state transitions (state machine)
                    
                    ## HTTP Response Codes
                    
                    - **200 OK**: Successful operation
                    - **201 Created**: Resource created successfully
                    - **400 Bad Request**: Validation error
                    - **404 Not Found**: Resource not found
                    - **409 Conflict**: Business conflict (overlap, vehicle unavailable)
                    - **422 Unprocessable Entity**: State transition not allowed
                    - **500 Internal Server Error**: Server error
                    """)
                .version("v2.0.0")
                .contact(new Contact()
                    .name("BFB Management Team")
                    .email("support@bfbmanagement.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local development server"),
                new Server()
                    .url("https://api.bfbmanagement.com")
                    .description("Production server")
            ));
    }
}
