package tn.esprit.agroprotect.microassurance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI/Swagger pour le service de microassurance
 * Disabled in unified mode to avoid duplicate OpenAPI bean conflicts
 */
@Configuration
@ConditionalOnProperty(name = "microassurance.swagger.enabled", havingValue = "true")
public class MicroassuranceOpenApiConfig {

    @Bean
    public OpenAPI microassuranceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AgroProtect Microassurance Service API")
                        .description("API pour la gestion des sinistres et indemnisations agricoles")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AgroProtect Team")
                                .email("agroprotect@esprit.tn")
                                .url("https://esprit.tn"))
                        .license(new License()
                                .name("AgroProtect License")
                                .url("https://esprit.tn/license")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT fourni par le service d'identité AgroProtect")));
    }
}