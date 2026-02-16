package tn.esprit.agroprotect.identity.service;

import tn.esprit.agroprotect.identity.dto.request.*;
import tn.esprit.agroprotect.identity.dto.response.AuthResponse;
import tn.esprit.agroprotect.identity.entity.*;
import tn.esprit.agroprotect.identity.exception.*;
import tn.esprit.agroprotect.identity.repository.*;
import tn.esprit.agroprotect.identity.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authentication service handling user registration, login, and token
 * management.
 * 
 * SECURITY FEATURES:
 * - BCrypt password hashing
 * - Account lockout after failed attempts
 * - Refresh token rotation
 * - Device binding
 * - Audit logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DeviceSessionRepository deviceSessionRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final AuditService auditService;

    @Value("${security.account.lock-threshold:5}")
    private int lockThreshold;

    @Value("${security.account.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Value("${security.password.history-count:5}")
    private int passwordHistoryCount;

    @Value("${jwt.access-token-ttl}")
    private long accessTokenTtl;

    /**
     * Register a new user with email/password.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            auditService.logAuthEvent(null, "REGISTER", AuditStatus.FAILURE,
                    "Email already exists", httpRequest);
            throw new EmailAlreadyExistsException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(UserStatus.PENDING) // Pending until email verified
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .phoneVerified(false)
                .consentMarketing(request.getConsentMarketing() != null && request.getConsentMarketing())
                .consentDataProcessing(true) // Required for registration
                .consentPolicyVersion(request.getConsentPolicyVersion())
                .consentAcceptedAt(Instant.now())
                .passwordChangedAt(Instant.now())
                .build();

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));
        user.addRole(userRole);

        user = userRepository.save(user);

        // Save password to history
        savePasswordToHistory(user.getId(), request.getPassword());

        // Generate and send email verification token
        sendEmailVerification(user);

        // Create device session
        String deviceId = request.getDeviceId() != null ? request.getDeviceId() : generateDeviceId();
        createDeviceSession(user, deviceId, httpRequest);

        // Generate tokens
        AuthResponse response = generateAuthResponse(user, deviceId);

        auditService.logAuthEvent(user.getId(), "REGISTER", AuditStatus.SUCCESS, null, httpRequest);

        log.info("User registered successfully: {}", user.getUuid());
        return response;
    }

    /**
     * Authenticate user with email/password.
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase();
        log.debug("Login attempt for email: {}", email);

        User user = userRepository.findActiveByEmailWithRoles(email)
                .orElseThrow(() -> {
                    auditService.logAuthEvent(null, "LOGIN", AuditStatus.FAILURE,
                            "User not found", httpRequest);
                    // Don't reveal if user exists
                    return new InvalidCredentialsException("Invalid email or password");
                });

        // Check if account is locked
        if (user.isLocked()) {
            auditService.logAuthEvent(user.getId(), "LOGIN", AuditStatus.BLOCKED,
                    "Account locked", httpRequest);
            throw new AccountLockedException("Account is locked. Please try again later.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, httpRequest);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Reset failed attempts on successful login
        user.resetFailedLoginAttempts();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Create or update device session
        String deviceId = request.getDeviceId() != null ? request.getDeviceId() : generateDeviceId();
        updateDeviceSession(user, deviceId, request.getDeviceName(), httpRequest);

        // Revoke old refresh tokens for this device
        refreshTokenRepository.revokeByUserAndDevice(
                user.getId(), deviceId, Instant.now(), "New login");

        // Generate new tokens
        AuthResponse response = generateAuthResponse(user, deviceId);

        auditService.logAuthEvent(user.getId(), "LOGIN", AuditStatus.SUCCESS, null, httpRequest);

        log.info("User logged in successfully: {}", user.getUuid());
        return response;
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String tokenHash = hashToken(request.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository
                .findValidToken(tokenHash, Instant.now())
                .orElseThrow(() -> {
                    auditService.logAuthEvent(null, "REFRESH_TOKEN", AuditStatus.FAILURE,
                            "Invalid or expired token", httpRequest);
                    throw new InvalidTokenException("Invalid or expired refresh token");
                });

        User user = refreshToken.getUser();

        // Check if user is still active
        if (!user.isActive()) {
            auditService.logAuthEvent(user.getId(), "REFRESH_TOKEN", AuditStatus.FAILURE,
                    "User not active", httpRequest);
            throw new InvalidTokenException("User account is not active");
        }

        // Revoke old token (rotation)
        refreshToken.revoke("Token rotated");
        refreshTokenRepository.save(refreshToken);

        // Generate new tokens with same device ID
        AuthResponse response = generateAuthResponse(user, refreshToken.getDeviceId());

        auditService.logAuthEvent(user.getId(), "REFRESH_TOKEN", AuditStatus.SUCCESS, null, httpRequest);

        return response;
    }

    /**
     * Logout current session.
     */
    @Transactional
    public void logout(String userUuid, String deviceId, HttpServletRequest httpRequest) {
        User user = userRepository.findActiveByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Revoke refresh tokens for this device
        refreshTokenRepository.revokeByUserAndDevice(
                user.getId(), deviceId, Instant.now(), "User logout");

        // Revoke device session
        deviceSessionRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                .ifPresent(session -> {
                    session.revoke();
                    deviceSessionRepository.save(session);
                });

        auditService.logAuthEvent(user.getId(), "LOGOUT", AuditStatus.SUCCESS, null, httpRequest);
        log.info("User logged out: {}", userUuid);
    }

    /**
     * Logout all sessions for user.
     */
    @Transactional
    public void logoutAll(String userUuid, HttpServletRequest httpRequest) {
        User user = userRepository.findActiveByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now(), "Logout all");

        // Revoke all device sessions
        deviceSessionRepository.revokeAllByUserId(user.getId(), Instant.now());

        auditService.logAuthEvent(user.getId(), "LOGOUT_ALL", AuditStatus.SUCCESS, null, httpRequest);
        log.info("All sessions logged out for user: {}", userUuid);
    }

    /**
     * Verify email with token.
     */
    @Transactional
    public void verifyEmail(String token) {
        String tokenHash = hashToken(token);

        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findValidToken(tokenHash, Instant.now())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

        // Mark token as used
        verificationToken.markAsUsed();
        emailVerificationTokenRepository.save(verificationToken);

        // Verify user email and activate account
        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getUuid());
    }

    /**
     * Request password reset.
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase();

        // Always return success to prevent email enumeration
        Optional<User> userOpt = userRepository.findActiveByEmail(email);

        if (userOpt.isEmpty()) {
            log.debug("Password reset requested for non-existent email: {}", email);
            return; // Don't reveal if user exists
        }

        User user = userOpt.get();

        // Invalidate any existing reset tokens
        passwordResetTokenRepository.invalidateAllByUserId(user.getId(), Instant.now());

        // Generate new reset token
        String token = generateSecureToken();
        String tokenHash = hashToken(token);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        auditService.logAuthEvent(user.getId(), "PASSWORD_RESET_REQUEST", AuditStatus.SUCCESS,
                null, httpRequest);

        log.info("Password reset email sent to: {}", email);
    }

    /**
     * Reset password with token.
     */
    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request, HttpServletRequest httpRequest) {
        String tokenHash = hashToken(request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidToken(tokenHash, Instant.now())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check password history
        validatePasswordNotReused(user.getId(), request.getNewPassword());

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        // Save to password history
        savePasswordToHistory(user.getId(), request.getNewPassword());

        // Mark token as used
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        // Invalidate all sessions (security measure)
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now(), "Password reset");
        deviceSessionRepository.revokeAllByUserId(user.getId(), Instant.now());

        auditService.logAuthEvent(user.getId(), "PASSWORD_RESET", AuditStatus.SUCCESS,
                null, httpRequest);

        log.info("Password reset successful for user: {}", user.getUuid());
    }

    // ==================== Helper Methods ====================

    private void handleFailedLogin(User user, HttpServletRequest httpRequest) {
        user.incrementFailedLoginAttempts();

        if (user.getFailedLoginAttempts() >= lockThreshold) {
            Instant lockUntil = Instant.now().plus(lockDurationMinutes, ChronoUnit.MINUTES);
            user.setLockedUntil(lockUntil);
            user.setStatus(UserStatus.LOCKED);
            log.warn("Account locked for user: {} until {}", user.getUuid(), lockUntil);
        }

        userRepository.save(user);
        auditService.logAuthEvent(user.getId(), "LOGIN", AuditStatus.FAILURE,
                "Invalid password", httpRequest);
    }

    private AuthResponse generateAuthResponse(User user, String deviceId) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getPermissionNames();

        String accessToken = jwtTokenProvider.generateAccessToken(
                null, user.getUuid(), deviceId, roles, permissions);

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUuid(), deviceId);

        // Store refresh token hash
        tokenService.saveRefreshToken(user, refreshToken, deviceId);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenTtl / 1000) // Convert to seconds
                .user(AuthResponse.UserResponse.builder()
                        .uuid(user.getUuid())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .emailVerified(user.getEmailVerified())
                        .phoneVerified(user.getPhoneVerified())
                        .mfaEnabled(user.getMfaEnabled())
                        .biometricEnabled(user.getBiometricEnabled())
                        .roles(roles)
                        .build())
                .build();
    }

    private void createDeviceSession(User user, String deviceId, HttpServletRequest request) {
        DeviceSession session = DeviceSession.builder()
                .user(user)
                .deviceId(deviceId)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .lastActiveAt(Instant.now())
                .isCurrent(true)
                .build();

        deviceSessionRepository.save(session);
    }

    private void updateDeviceSession(User user, String deviceId, String deviceName,
            HttpServletRequest request) {
        DeviceSession session = deviceSessionRepository
                .findByUserIdAndDeviceId(user.getId(), deviceId)
                .orElseGet(() -> DeviceSession.builder()
                        .user(user)
                        .deviceId(deviceId)
                        .build());

        session.setDeviceName(deviceName);
        session.setIpAddress(getClientIp(request));
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setLastActiveAt(Instant.now());
        session.setRevoked(false);
        session.setRevokedAt(null);

        deviceSessionRepository.save(session);
    }

    private void sendEmailVerification(User user) {
        String token = generateSecureToken();
        String tokenHash = hashToken(token);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    private void savePasswordToHistory(Long userId, String rawPassword) {
        PasswordHistory history = PasswordHistory.builder()
                .userId(userId)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();

        passwordHistoryRepository.save(history);

        // Keep only last N passwords
        List<PasswordHistory> historyList = passwordHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        if (historyList.size() > passwordHistoryCount) {
            List<PasswordHistory> toDelete = historyList.subList(passwordHistoryCount, historyList.size());
            passwordHistoryRepository.deleteAll(toDelete);
        }
    }

    private void validatePasswordNotReused(Long userId, String newPassword) {
        List<PasswordHistory> recentPasswords = passwordHistoryRepository
                .findRecentByUserId(userId, passwordHistoryCount);

        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new PasswordReusedException(
                        "Cannot reuse recent passwords. Please choose a different password.");
            }
        }
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
