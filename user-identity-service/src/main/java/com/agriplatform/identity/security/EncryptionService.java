package com.agriplatform.identity.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM Encryption Service for biometric data.
 * 
 * CRITICAL SECURITY NOTES:
 * - Uses AES-256-GCM (authenticated encryption)
 * - IV is randomly generated for each encryption operation
 * - Authentication tag is included in the ciphertext
 * - Secret key must be 256 bits (32 bytes) base64-encoded
 */
@Component
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public EncryptionService(@Value("${security.aes-secret-key}") String aesKeyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(aesKeyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES key must be 256 bits (32 bytes)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encrypt data using AES-256-GCM.
     * 
     * @param plaintext The data to encrypt
     * @return EncryptionResult containing IV, ciphertext, and auth tag
     */
    public EncryptionResult encrypt(byte[] plaintext) {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext);

            // The ciphertext includes the authentication tag at the end
            // Split them for separate storage
            int tagOffset = ciphertext.length - (GCM_TAG_LENGTH / 8);
            byte[] encryptedData = new byte[tagOffset];
            byte[] authTag = new byte[GCM_TAG_LENGTH / 8];

            System.arraycopy(ciphertext, 0, encryptedData, 0, tagOffset);
            System.arraycopy(ciphertext, tagOffset, authTag, 0, authTag.length);

            return new EncryptionResult(
                    encryptedData,
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(authTag));

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt data using AES-256-GCM.
     * 
     * @param encryptedData The encrypted data
     * @param ivBase64      The IV used for encryption (base64)
     * @param authTagBase64 The authentication tag (base64)
     * @return Decrypted plaintext
     */
    public byte[] decrypt(byte[] encryptedData, String ivBase64, String authTagBase64) {
        try {
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] authTag = Base64.getDecoder().decode(authTagBase64);

            // Reconstruct ciphertext with auth tag
            byte[] ciphertext = new byte[encryptedData.length + authTag.length];
            System.arraycopy(encryptedData, 0, ciphertext, 0, encryptedData.length);
            System.arraycopy(authTag, 0, ciphertext, encryptedData.length, authTag.length);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            // Decrypt
            return cipher.doFinal(ciphertext);

        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Encryption result containing ciphertext, IV, and authentication tag.
     */
    public record EncryptionResult(
            byte[] ciphertext,
            String ivBase64,
            String authTagBase64) {
    }

    /**
     * Encrypt a string to a single base64-encoded result.
     * Format: IV (12 bytes) + Ciphertext + Tag (16 bytes), all base64 encoded.
     *
     * @param plaintext The string to encrypt
     * @return Base64-encoded encrypted data
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Combine IV + ciphertext (which includes auth tag)
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a base64-encoded encrypted string.
     *
     * @param encryptedBase64 Base64-encoded encrypted data (IV + ciphertext + tag)
     * @return Decrypted string
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

            // Extract ciphertext (includes auth tag)
            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
