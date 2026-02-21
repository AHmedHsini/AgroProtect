package tn.esprit.agroprotect;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AgroProtect Unified Application
 * 
 * Unified entry point for AgroProtect microservices platform including:
 * - Identity Service: User authentication, JWT, role-based access
 * - Microassurance Service: Claims and payouts management
 * 
 * Both services run under a single Spring Boot application on port 8080
 */
@SpringBootApplication(scanBasePackages = {
    "tn.esprit.agroprotect.identity",
    "tn.esprit.agroprotect.microassurance"
})
@EnableJpaAuditing
@EnableAsync
public class AgroProtectApplication {

    public static void main(String[] args) {
        // Load .env file before Spring starts
        loadEnvVariables();

        SpringApplication.run(AgroProtectApplication.class, args);
    }

    /**
     * Load environment variables from .env file
     */
    private static void loadEnvVariables() {
        try {
            Dotenv dotenv = Dotenv.load();
            // Environment variables are automatically picked up
        } catch (Exception e) {
            System.out.println("No .env file found or error loading it. Using default configuration.");
        }
    }
}
