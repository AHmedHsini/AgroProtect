package tn.esprit.agroprotect.microassurance.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to lookup user information from the identity microservice
 * This bridges the microassurance service with the identity service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserLookupService {

    private final RestTemplate restTemplate;
    
    // Simple in-memory cache for user UUID -> ID mapping
    private final Map<String, Long> userIdCache = new HashMap<>();

    /**
     * Get user ID from user UUID by calling the identity service
     * FIXME: This is a temporary implementation using cached/test values
     * In production, should implement proper caching with TTL
     */
    public Long getUserIdByUuid(String userUuid) {
        if (userUuid == null || userUuid.isEmpty()) {
            log.warn("Received null or empty user UUID");
            return 1L;  // Test fallback
        }

        // Check cache first
        if (userIdCache.containsKey(userUuid)) {
            return userIdCache.get(userUuid);
        }

        try {
            // In production: call the identity service to resolve UUID to ID
            // For now, use a simple mapping
            // TODO: Implement proper call to /v1/admin/users/{uuid}/internal
            log.debug("User UUID: {} not in cache, would call identity service", userUuid);
            
            // Temporary: Generate a simple ID from UUID hash
            long userId = Math.abs(userUuid.hashCode()) % 1000000L;
            if (userId == 0) userId = 1L;
            
            userIdCache.put(userUuid, userId);
            return userId;
            
        } catch (Exception e) {
            log.error("Failed to lookup user ID for UUID: {}", userUuid, e);
            return 1L;  // Fallback
        }
    }

    /**
     * Clear the cache (useful for testing)
     */
    public void clearCache() {
        userIdCache.clear();
    }
}
