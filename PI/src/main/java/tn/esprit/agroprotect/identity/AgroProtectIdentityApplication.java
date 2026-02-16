package tn.esprit.agroprotect.identity;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * User & Identity Microservice Application
 * 
 * This microservice handles all identity-related operations for the
 * AgriPlatform:
 * - User registration and authentication
 * - JWT token management
 * - OAuth2 (Google) integration
 * - Biometric face recognition
 * - Role-based access control
 * - Audit logging
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class AgroProtectIdentityApplication {

    public static void main(String[] args) {
        // Load .env file before Spring starts
        loadEnvVariables();

        SpringApplication.run(AgroProtectIdentityApplication.class, args);
    }

    private static void loadEnvVariables() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null
                        && System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        } catch (Exception e) {
            System.out.println("No .env file found, using system environment variables");
        }
    }
}
