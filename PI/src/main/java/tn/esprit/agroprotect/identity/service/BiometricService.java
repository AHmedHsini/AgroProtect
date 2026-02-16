package tn.esprit.agroprotect.identity.service;

import tn.esprit.agroprotect.identity.dto.request.BiometricEnrollRequest;
import tn.esprit.agroprotect.identity.dto.request.BiometricVerifyRequest;
import tn.esprit.agroprotect.identity.dto.response.BiometricResponse;
import tn.esprit.agroprotect.identity.entity.*;
import tn.esprit.agroprotect.identity.exception.BiometricException;
import tn.esprit.agroprotect.identity.exception.UserNotFoundException;
import tn.esprit.agroprotect.identity.repository.BiometricDataRepository;
import tn.esprit.agroprotect.identity.repository.UserRepository;
import tn.esprit.agroprotect.identity.security.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Biometric enrollment and verification service.
 * 
 * SECURITY:
 * - Face embeddings are encrypted with AES-256-GCM before storage
 * - Liveness detection is required for enrollment
 * - Verification requires minimum confidence threshold
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BiometricService {

        private final UserRepository userRepository;
        private final BiometricDataRepository biometricDataRepository;
        private final EncryptionService encryptionService;
        private final MlFaceServiceClient mlFaceServiceClient;
        private final AuditService auditService;

        private static final double VERIFICATION_THRESHOLD = 0.85;
        private static final double LIVENESS_THRESHOLD = 0.90;

        /**
         * Enroll face biometric for user.
         */
        @Transactional
        public BiometricResponse enrollFace(String userUuid, BiometricEnrollRequest request,
                        HttpServletRequest httpRequest) {
                log.info("Enrolling face for user: {}", userUuid);

                User user = userRepository.findActiveByUuid(userUuid)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                // Check if already enrolled
                Optional<BiometricData> existing = biometricDataRepository
                                .findActiveByUserIdAndType(user.getId(), BiometricType.FACE);

                if (existing.isPresent()) {
                        throw new BiometricException("Face already enrolled. Remove existing before re-enrolling.");
                }

                // Call ML service to extract face embedding
                MlFaceServiceClient.FaceExtractionResult extractionResult = mlFaceServiceClient
                                .extractFaceEmbedding(request.getFaceImage());

                if (!extractionResult.success()) {
                        auditService.logUserAction(user.getId(), user.getId(), "BIOMETRIC_ENROLL",
                                        "BIOMETRIC", null, AuditStatus.FAILURE, null, httpRequest);
                        throw new BiometricException("Face detection failed: " + extractionResult.errorMessage());
                }

                // Verify liveness
                if (extractionResult.livenessScore() < LIVENESS_THRESHOLD) {
                        auditService.logUserAction(user.getId(), user.getId(), "BIOMETRIC_ENROLL",
                                        "BIOMETRIC", null, AuditStatus.FAILURE, null, httpRequest);
                        throw new BiometricException(
                                        "Liveness check failed. Please ensure you're using a live camera.");
                }

                // Encrypt the embedding before storage
                EncryptionService.EncryptionResult encryptionResult = encryptionService.encrypt(
                    extractionResult.embedding().getBytes(java.nio.charset.StandardCharsets.UTF_8));

                // Save biometric data
                BiometricData biometricData = BiometricData.builder()
                                .user(user)
                                .biometricType(BiometricType.FACE)
                                .embeddingEncrypted(encryptionResult.ciphertext())
                                .encryptionIv(encryptionResult.ivBase64())
                                .encryptionTag(encryptionResult.authTagBase64())
                                .qualityScore(java.math.BigDecimal.valueOf(extractionResult.qualityScore()))
                                .isActive(true)
                                .enrolledAt(Instant.now())
                                .build();

                biometricDataRepository.save(biometricData);

                // Update user biometric status
                user.setBiometricEnabled(true);
                userRepository.save(user);

                auditService.logUserAction(user.getId(), user.getId(), "BIOMETRIC_ENROLL",
                                "BIOMETRIC", biometricData.getId().toString(), AuditStatus.SUCCESS, null, httpRequest);

                log.info("Face enrolled successfully for user: {}", userUuid);

                return BiometricResponse.builder()
                                .verified(true)
                                .confidenceScore(extractionResult.qualityScore())
                                .livenessVerified(true)
                                .message("Face enrolled successfully")
                                .build();
        }

        /**
         * Verify face against enrolled biometric.
         */
        @Transactional(readOnly = true)
        public BiometricResponse verifyFace(BiometricVerifyRequest request,
                        HttpServletRequest httpRequest) {
                log.debug("Verifying face for user: {}", request.getUserUuid());

                User user = userRepository.findActiveByUuid(request.getUserUuid())
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                BiometricData biometricData = biometricDataRepository
                                .findActiveByUserIdAndType(user.getId(), BiometricType.FACE)
                                .orElseThrow(() -> new BiometricException("No face enrollment found"));

                // Extract embedding from verification image
                MlFaceServiceClient.FaceExtractionResult extractionResult = mlFaceServiceClient
                                .extractFaceEmbedding(request.getFaceImage());

                if (!extractionResult.success()) {
                        auditService.logUserAction(user.getId(), null, "BIOMETRIC_VERIFY",
                                        "BIOMETRIC", biometricData.getId().toString(), AuditStatus.FAILURE, null,
                                        httpRequest);
                        return BiometricResponse.builder()
                                        .verified(false)
                                        .message("Face detection failed")
                                        .build();
                }

                // Decrypt enrolled embedding
                byte[] decryptedBytes = encryptionService.decrypt(
                    biometricData.getEmbeddingEncrypted(), 
                    biometricData.getEncryptionIv(), 
                    biometricData.getEncryptionTag());
                String enrolledEmbedding = new String(decryptedBytes, java.nio.charset.StandardCharsets.UTF_8);

                // Compare embeddings
                MlFaceServiceClient.FaceComparisonResult comparisonResult = mlFaceServiceClient.compareFaces(
                                enrolledEmbedding,
                                extractionResult.embedding());

                boolean verified = comparisonResult.similarity() >= VERIFICATION_THRESHOLD;

                // Update last verification
                if (verified) {
                        biometricData.setLastVerifiedAt(Instant.now());
                        biometricDataRepository.save(biometricData);
                }

                auditService.logUserAction(user.getId(), null, "BIOMETRIC_VERIFY",
                                "BIOMETRIC", biometricData.getId().toString(),
                                verified ? AuditStatus.SUCCESS : AuditStatus.FAILURE, null, httpRequest);

                return BiometricResponse.builder()
                                .verified(verified)
                                .confidenceScore(comparisonResult.similarity())
                                .livenessVerified(extractionResult.livenessScore() >= LIVENESS_THRESHOLD)
                                .message(verified ? "Face verified" : "Face did not match")
                                .build();
        }

        /**
         * Remove biometric enrollment.
         */
        @Transactional
        public void removeBiometric(String userUuid, HttpServletRequest httpRequest) {
                User user = userRepository.findActiveByUuid(userUuid)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                biometricDataRepository.findActiveByUserIdAndType(user.getId(), BiometricType.FACE)
                                .ifPresent(biometric -> {
                                        biometric.setActive(false);
                                        biometricDataRepository.save(biometric);
                                });

                user.setBiometricEnabled(false);
                userRepository.save(user);

                auditService.logUserAction(user.getId(), user.getId(), "BIOMETRIC_REMOVE",
                                "BIOMETRIC", null, AuditStatus.SUCCESS, null, httpRequest);

                log.info("Biometric removed for user: {}", userUuid);
        }

        /**
         * Check if biometric is enrolled.
         */
        @Transactional(readOnly = true)
        public boolean isBiometricEnrolled(String userUuid) {
                User user = userRepository.findActiveByUuid(userUuid)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                return biometricDataRepository
                                .findActiveByUserIdAndType(user.getId(), BiometricType.FACE)
                                .isPresent();
        }
}
