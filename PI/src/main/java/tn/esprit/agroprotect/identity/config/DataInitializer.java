package tn.esprit.agroprotect.identity.config;

import tn.esprit.agroprotect.identity.entity.Role;
import tn.esprit.agroprotect.identity.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Data initialization for development environment.
 * Creates default roles and permissions for testing.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            log.info("Initializing development data...");

            // Create default roles if they don't exist
            createRoleIfNotExists("ADMIN", "System administrator with full access", true);
            createRoleIfNotExists("FARMER", "Agricultural farmer user", true);
            createRoleIfNotExists("INVESTOR", "Microfinance investor", true);
            createRoleIfNotExists("INSURER", "Insurance provider representative", true);
            createRoleIfNotExists("EXPERT", "Agricultural and financial expert advisor", true);
            createRoleIfNotExists("WORKER", "Agricultural worker/laborer", true);
            createRoleIfNotExists("USER", "Basic authenticated user", true);

            log.info("Data initialization completed successfully");
        };
    }

    private void createRoleIfNotExists(String roleName, String description, boolean isSystemRole) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .isSystemRole(isSystemRole)
                    .build();
            roleRepository.save(role);
            log.debug("Created role: {}", roleName);
        }
    }
}
