package com.agriplatform.identity.security;

import com.agriplatform.identity.config.JwtConfig.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Token Provider using RS256 asymmetric algorithm.
 * 
 * SECURITY CONSIDERATIONS:
 * - Uses RS256 (asymmetric) to enable token verification without private key
 * - Access tokens are short-lived (15 min default)
 * - Refresh tokens are long-lived but stored hashed in DB
 * - Tokens include device binding for session control
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final JwtProperties jwtProperties;

    /**
     * Generate access token with user claims.
     */
    public String generateAccessToken(UserDetails userDetails, String userUuid, String deviceId,
            Set<String> roles, Set<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("device_id", deviceId);
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        return buildToken(claims, userUuid, jwtProperties.accessTokenTtl());
    }

    /**
     * Generate refresh token (minimal claims, stored hashed in DB).
     */
    public String generateRefreshToken(String userUuid, String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("device_id", deviceId);
        claims.put("jti", UUID.randomUUID().toString()); // Unique token ID

        return buildToken(claims, userUuid, jwtProperties.refreshTokenTtl());
    }

    /**
     * Generate service-to-service token.
     */
    public String generateServiceToken(String serviceName, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "service");
        claims.put("service_name", serviceName);
        claims.put("permissions", permissions);

        return buildToken(claims, serviceName, jwtProperties.accessTokenTtl() * 4); // 1 hour
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long ttlMillis) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(ttlMillis);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Extract username (subject) from token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract token type (access/refresh/service).
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extract device ID from token.
     */
    public String extractDeviceId(String token) {
        return extractClaim(token, claims -> claims.get("device_id", String.class));
    }

    /**
     * Extract roles from token.
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        return roles != null ? new HashSet<>(roles) : Collections.emptySet();
    }

    /**
     * Extract permissions from token.
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractPermissions(String token) {
        List<String> perms = extractClaim(token, claims -> claims.get("permissions", List.class));
        return perms != null ? new HashSet<>(perms) : Collections.emptySet();
    }

    /**
     * Extract expiration date from token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract JTI (JWT ID) from token.
     */
    public String extractJti(String token) {
        return extractClaim(token, claims -> claims.get("jti", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Simple boolean token validation.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate token with comprehensive checks.
     */
    public TokenValidationResult validateTokenFull(String token) {
        try {
            Claims claims = extractAllClaims(token);

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                return TokenValidationResult.expired();
            }

            // Check token type
            String tokenType = claims.get("type", String.class);
            if (tokenType == null) {
                return TokenValidationResult.invalid("Missing token type");
            }

            return TokenValidationResult.valid(claims.getSubject(), tokenType);

        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return TokenValidationResult.expired();
        } catch (SignatureException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            return TokenValidationResult.invalid("Invalid signature");
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            return TokenValidationResult.invalid("Malformed token");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported token: {}", e.getMessage());
            return TokenValidationResult.invalid("Unsupported token format");
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return TokenValidationResult.invalid("Validation error");
        }
    }

    /**
     * Check if token is valid for the given user.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        TokenValidationResult result = validateTokenFull(token);
        return result.isValid() && result.subject().equals(userDetails.getUsername());
    }

    /**
     * Get user UUID from token (alias for extractUsername).
     */
    public String getUserUuidFromToken(String token) {
        return extractUsername(token);
    }

    /**
     * Get roles from token.
     */
    public Set<String> getRolesFromToken(String token) {
        return extractRoles(token);
    }

    /**
     * Get permissions from token.
     */
    public Set<String> getPermissionsFromToken(String token) {
        return extractPermissions(token);
    }

    /**
     * Get user ID from security context (returns null - admin uses UUID).
     */
    public Long getUserIdFromContext() {
        return null;
    }

    /**
     * Token validation result record.
     */
    public record TokenValidationResult(
            boolean isValid,
            boolean isExpired,
            String subject,
            String tokenType,
            String errorMessage) {
        public static TokenValidationResult valid(String subject, String tokenType) {
            return new TokenValidationResult(true, false, subject, tokenType, null);
        }

        public static TokenValidationResult expired() {
            return new TokenValidationResult(false, true, null, null, "Token expired");
        }

        public static TokenValidationResult invalid(String errorMessage) {
            return new TokenValidationResult(false, false, null, null, errorMessage);
        }
    }
}
