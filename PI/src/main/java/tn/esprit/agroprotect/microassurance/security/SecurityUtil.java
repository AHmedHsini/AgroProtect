package tn.esprit.agroprotect.microassurance.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilitaire pour extraire les informations de sécurité du contexte
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserLookupService userLookupService;

    /**
     * Récupère l'ID de l'utilisateur depuis le token JWT
     * Extracts UUID from JWT subject claim and resolves it to a database user ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If no authentication (testing), return a test user ID
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return 1L;  // TEMPORARY: Test user ID
        }
        
        // Get user UUID from 'sub' claim (subject)
        String userUuid = jwt.getSubject();
        
        if (userUuid == null || userUuid.isEmpty()) {
            return 1L;  // TEMPORARY: Fallback test user ID
        }
        
        // Use UserLookupService to resolve UUID to database ID
        return userLookupService.getUserIdByUuid(userUuid);
    }

    /**
     * Récupère l'UUID de l'utilisateur depuis le token JWT
     */
    public String getCurrentUserUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        
        return jwt.getSubject();
    }

    /**
     * Récupère les rôles de l'utilisateur depuis le token JWT
     */
    public Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }

    /**
     * Vérifie si l'utilisateur actuel a un rôle spécifique
     */
    public boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Vérifie si l'utilisateur actuel est un administrateur
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Vérifie si l'utilisateur actuel est un expert
     */
    public boolean isExpert() {
        return hasRole("EXPERT");
    }

    /**
     * Vérifie si l'utilisateur actuel est un agriculteur
     */
    public boolean isAgriculteur() {
        return hasRole("AGRICULTEUR");
    }

    /**
     * Vérifie si l'utilisateur actuel peut voir tous les sinistres
     */
    public boolean canViewAllSinistres() {
        return isAdmin() || isExpert();
    }

    /**
     * Vérifie si l'utilisateur actuel peut modifier le statut des sinistres
     */
    public boolean canModifySinistreStatus() {
        return isAdmin() || isExpert();
    }

    /**
     * Vérifie si l'utilisateur actuel peut créer des indemnisations
     */
    public boolean canCreateIndemnisation() {
        return isAdmin();
    }

    /**
     * Vérifie si l'utilisateur actuel peut effectuer des paiements
     */
    public boolean canProcessPayments() {
        return isAdmin();
    }

    /**
     * Vérifie si l'utilisateur actuel est le propriétaire d'un sinistre
     */
    public boolean isOwnerOf(Long createdByUserId) {
        return getCurrentUserId().equals(createdByUserId);
    }

    /**
     * Vérifie si l'utilisateur actuel peut voir un sinistre spécifique
     */
    public boolean canViewSinistre(Long createdByUserId) {
        return canViewAllSinistres() || isOwnerOf(createdByUserId);
    }
}