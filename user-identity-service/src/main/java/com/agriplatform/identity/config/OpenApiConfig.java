package com.agriplatform.identity.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger documentation configuration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User & Identity Service API")
                        .version("1.0.0")
                        .description("""
                                User and Identity Management Microservice for AgriPlatform.

                                This service handles:
                                - User registration and authentication
                                - JWT token management (RS256)
                                - OAuth2 (Google) integration
                                - Biometric face recognition
                                - Role-based access control
                                - Session and device management
                                - Audit logging
                                """)
                        .contact(new Contact()
                                .name("AgriPlatform Team")
                                .email("support@agriplatform.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://agriplatform.com/license")))
                .servers(List.of(
                        new Server().url("/api").description("API Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token authentication")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
