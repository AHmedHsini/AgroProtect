package tn.esprit.agroprotect.identity.service;

import tn.esprit.agroprotect.identity.entity.RefreshToken;
import tn.esprit.agroprotect.identity.entity.User;
import tn.esprit.agroprotect.identity.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Token management service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-ttl}")
    private long refreshTokenTtl;

    /**
     * Save refresh token hash to database.
     */
    @Transactional
    public void saveRefreshToken(User user, String rawToken, String deviceId) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .deviceId(deviceId)
                .expiresAt(Instant.now().plusMillis(refreshTokenTtl))
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoke all tokens for a user.
     */
    @Transactional
    public void revokeAllUserTokens(Long userId, String reason) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now(), reason);
        log.info("Revoked all tokens for user: {}", userId);
    }

    /**
     * Clean up expired and revoked tokens.
     * Runs every day at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Cleaned up {} expired/revoked tokens", deleted);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
