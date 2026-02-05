package com.agriplatform.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * JWT configuration for RS256 asymmetric key signing.
 * 
 * SECURITY: Keys must be loaded from environment variables.
 * Generate keys with:
 * openssl genrsa -out private.pem 2048
 * openssl rsa -in private.pem -pubout -out public.pem
 * (Convert to single-line Base64 for environment variables)
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.private-key}")
    private String privateKeyBase64;

    @Value("${jwt.public-key}")
    private String publicKeyBase64;

    @Value("${jwt.access-token-ttl}")
    private long accessTokenTtl;

    @Value("${jwt.refresh-token-ttl}")
    private long refreshTokenTtl;

    @Value("${jwt.issuer}")
    private String issuer;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        // privateKeyBase64 is already base64-encoded PEM format (with headers)
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyBase64);
        String keyString = new String(decodedKey);
        
        // Strip PEM headers and whitespace
        String privateKeyPEM = keyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        // publicKeyBase64 is already base64-encoded PEM format (with headers)
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyBase64);
        String keyString = new String(decodedKey);
        
        // Strip PEM headers and whitespace
        String publicKeyPEM = keyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean
    public JwtProperties jwtProperties() {
        return new JwtProperties(accessTokenTtl, refreshTokenTtl, issuer);
    }

    public record JwtProperties(long accessTokenTtl, long refreshTokenTtl, String issuer) {
    }
}
