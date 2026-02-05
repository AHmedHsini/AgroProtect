package com.agriplatform.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Biometric data entity for face recognition storage.
 * 
 * CRITICAL SECURITY NOTES:
 * - Stores only encrypted face embeddings (128-dimensional vectors)
 * - NO raw face images are ever stored
 * - Encryption uses AES-256-GCM with unique IV per record
 * - Liveness detection must be verified before enrollment
 */
@Entity
@Table(name = "biometric_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiometricData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "biometric_type")
    @Builder.Default
    private BiometricType biometricType = BiometricType.FACE;

    /**
     * AES-256-GCM encrypted face embedding.
     * The embedding is a 128-dimensional vector from the face recognition model.
     */
    @Lob
    @Column(name = "embedding_encrypted", nullable = false)
    private byte[] embeddingEncrypted;

    /**
     * Initialization vector used for AES-GCM encryption.
     * Must be unique per encryption operation.
     */
    @Column(name = "encryption_iv", nullable = false, length = 32)
    private String encryptionIv;

    /**
     * Authentication tag from AES-GCM (for integrity verification).
     */
    @Column(name = "encryption_tag", nullable = false, length = 32)
    private String encryptionTag;

    /**
     * Indicates if liveness was verified during enrollment.
     */
    @Column(name = "liveness_verified")
    @Builder.Default
    private Boolean livenessVerified = false;

    /**
     * Quality score of the enrolled face image (0.0 to 1.0).
     */
    @Column(name = "quality_score", precision = 5, scale = 4)
    private BigDecimal qualityScore;

    @Column(name = "enrolled_at")
    @CreationTimestamp
    private Instant enrolledAt;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @Column(name = "verification_count")
    @Builder.Default
    private Integer verificationCount = 0;

    @Column(name = "failed_verification_count")
    @Builder.Default
    private Integer failedVerificationCount = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public void recordSuccessfulVerification() {
        this.lastVerifiedAt = Instant.now();
        this.verificationCount = (this.verificationCount == null ? 0 : this.verificationCount) + 1;
    }

    public void recordFailedVerification() {
        this.failedVerificationCount = (this.failedVerificationCount == null ? 0 : this.failedVerificationCount) + 1;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
