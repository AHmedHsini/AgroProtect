package tn.esprit.agroprotect.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Simplified JWT configuration using HMAC for development.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret-key:agroprotect-dev-secret-key-2026-super-secure-hmac-key}")
    private String secretKey;

    @Value("${jwt.access-token-ttl}")
    private long accessTokenTtl;

    @Value("${jwt.refresh-token-ttl}")
    private long refreshTokenTtl;

    @Value("${jwt.issuer}")
    private String issuer;

    @Bean
    public SecretKey jwtSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Ensure key is at least 32 bytes for HS256
            String paddedKey = secretKey + "0".repeat(32 - keyBytes.length);
            keyBytes = paddedKey.getBytes(StandardCharsets.UTF_8);
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtProperties jwtProperties() {
        return new JwtProperties(accessTokenTtl, refreshTokenTtl, issuer);
    }

    public record JwtProperties(long accessTokenTtl, long refreshTokenTtl, String issuer) {
    }
}
