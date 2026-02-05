package com.agriplatform.identity.service;

import com.agriplatform.identity.dto.request.UpdateProfileRequest;
import com.agriplatform.identity.dto.request.ChangePasswordRequest;
import com.agriplatform.identity.dto.response.DeviceSessionResponse;
import com.agriplatform.identity.dto.response.UserProfileResponse;
import com.agriplatform.identity.entity.*;
import com.agriplatform.identity.exception.*;
import com.agriplatform.identity.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User management service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeviceSessionRepository deviceSessionRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final EmailService emailService;

    @Value("${security.password.history-count:5}")
    private int passwordHistoryCount;

    /**
     * Get current user profile.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String userUuid) {
        User user = userRepository.findActiveByUuidWithRoles(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapToProfileResponse(user);
    }

    /**
     * Update user profile.
     */
    @Transactional
    public UserProfileResponse updateProfile(String userUuid, UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        User user = userRepository.findActiveByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // Phone changed - mark as unverified
            user.setPhone(request.getPhone());
            user.setPhoneVerified(false);
        }
        if (request.getConsentMarketing() != null) {
            user.setConsentMarketing(request.getConsentMarketing());
        }

        user = userRepository.save(user);

        auditService.logUserAction(user.getId(), user.getId(), "UPDATE_PROFILE",
                "USER", user.getUuid(), AuditStatus.SUCCESS, null, httpRequest);

        return mapToProfileResponse(user);
    }

    /**
     * Change password.
     */
    @Transactional
    public void changePassword(String userUuid, ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        User user = userRepository.findActiveByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            auditService.logUserAction(user.getId(), user.getId(), "CHANGE_PASSWORD",
                    "USER", user.getUuid(), AuditStatus.FAILURE, null, httpRequest);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Check password history
        validatePasswordNotReused(user.getId(), request.getNewPassword());

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        // Save to history
        savePasswordToHistory(user.getId(), request.getNewPassword());

        // Revoke all other sessions except current
        String currentDeviceId = (String) httpRequest.getAttribute("deviceId");
        if (currentDeviceId != null) {
            Long currentSessionId = deviceSessionRepository
                    .findByUserIdAndDeviceId(user.getId(), currentDeviceId)
                    .map(DeviceSession::getId)
                    .orElse(null);

            if (currentSessionId != null) {
                deviceSessionRepository.revokeOtherSessions(user.getId(), currentSessionId, Instant.now());
            }
        }

        auditService.logUserAction(user.getId(), user.getId(), "CHANGE_PASSWORD",
                "USER", user.getUuid(), AuditStatus.SUCCESS, null, httpRequest);

        // Send security alert
        emailService.sendSecurityAlertEmail(user.getEmail(), "Password Changed",
                "Your password was changed. If this was not you, please contact support immediately.");
    }

    /**
     * Get user's active sessions.
     */
    @Transactional(readOnly = true)
    public List<DeviceSessionResponse> getUserSessions(String userUuid, String currentDeviceId) {
        User user = userRepository.findActiveByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return deviceSessionRepository.findActiveSessionsByUser(user.getId())
                .stream()
                .map(session -> mapToSessionResponse(session,
                        session.getDeviceId().equals(currentDeviceId)))
                .collect(Collectors.toList());
    }

    /**
     * Revoke a specific session.
     */
    @Transactional
    public void revokeSession(String userUuid, Long sessionId, HttpServletRequest httpRequest) {
        User user = userRepository.findActiveByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        DeviceSession session = deviceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Session does not belong to user");
        }

        // Revoke session
        session.revoke();
        deviceSessionRepository.save(session);

        // Revoke associated refresh tokens
        refreshTokenRepository.revokeByUserAndDevice(
                user.getId(), session.getDeviceId(), Instant.now(), "Session revoked");

        auditService.logUserAction(user.getId(), user.getId(), "REVOKE_SESSION",
                "SESSION", sessionId.toString(), AuditStatus.SUCCESS, null, httpRequest);
    }

    /**
     * Request account deletion (GDPR).
     */
    @Transactional
    public void requestAccountDeletion(String userUuid, HttpServletRequest httpRequest) {
        User user = userRepository.findActiveByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        // Revoke all sessions
        refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now(), "Account deleted");
        deviceSessionRepository.revokeAllByUserId(user.getId(), Instant.now());

        auditService.logUserAction(user.getId(), user.getId(), "REQUEST_DELETION",
                "USER", user.getUuid(), AuditStatus.SUCCESS, null, httpRequest);

        log.info("Account deletion requested for user: {}", userUuid);
    }

    // ==================== Admin Methods ====================

    /**
     * Get user by UUID (admin).
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserByUuid(String uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapToProfileResponse(user);
    }

    /**
     * Search users (admin).
     */
    @Transactional(readOnly = true)
    public Page<UserProfileResponse> searchUsers(UserStatus status, String search, Pageable pageable) {
        return userRepository.searchUsers(status, search, pageable)
                .map(this::mapToProfileResponse);
    }

    /**
     * Update user status (admin).
     */
    @Transactional
    public void updateUserStatus(String uuid, UserStatus newStatus, Long actorId,
            HttpServletRequest httpRequest) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);

        if (newStatus == UserStatus.DISABLED || newStatus == UserStatus.LOCKED) {
            // Revoke all sessions
            refreshTokenRepository.revokeAllByUserId(user.getId(), Instant.now(),
                    "Account " + newStatus.name().toLowerCase());
            deviceSessionRepository.revokeAllByUserId(user.getId(), Instant.now());
        }

        userRepository.save(user);

        auditService.logUserAction(user.getId(), actorId, "UPDATE_STATUS",
                "USER", uuid, AuditStatus.SUCCESS, null, httpRequest);

        log.info("User {} status changed from {} to {} by admin", uuid, oldStatus, newStatus);
    }

    /**
     * Assign roles to user (admin).
     */
    @Transactional
    public void assignRoles(String uuid, Set<String> roleNames, Long actorId,
            HttpServletRequest httpRequest) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Clear existing roles
        user.getRoles().clear();

        // Add new roles
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            user.addRole(role);
        }

        userRepository.save(user);

        auditService.logUserAction(user.getId(), actorId, "ASSIGN_ROLES",
                "USER", uuid, AuditStatus.SUCCESS, null, httpRequest);

        log.info("Roles {} assigned to user {} by admin", roleNames, uuid);
    }

    // ==================== Helper Methods ====================

    private UserProfileResponse mapToProfileResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .authProvider(user.getAuthProvider().name())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .mfaEnabled(user.getMfaEnabled())
                .biometricEnabled(user.getBiometricEnabled())
                .roles(roles)
                .permissions(user.getPermissionNames())
                .consentMarketing(user.getConsentMarketing())
                .consentPolicyVersion(user.getConsentPolicyVersion())
                .consentAcceptedAt(user.getConsentAcceptedAt())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private DeviceSessionResponse mapToSessionResponse(DeviceSession session, boolean isCurrent) {
        return DeviceSessionResponse.builder()
                .id(session.getId())
                .deviceId(session.getDeviceId())
                .deviceName(session.getDeviceName())
                .deviceType(session.getDeviceType() != null ? session.getDeviceType().name() : null)
                .osName(session.getOsName())
                .browserName(session.getBrowserName())
                .ipAddress(session.getIpAddress())
                .locationCountry(session.getLocationCountry())
                .locationCity(session.getLocationCity())
                .isCurrent(isCurrent)
                .isTrusted(session.getIsTrusted())
                .lastActiveAt(session.getLastActiveAt())
                .createdAt(session.getCreatedAt())
                .build();
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

    private void savePasswordToHistory(Long userId, String rawPassword) {
        PasswordHistory history = PasswordHistory.builder()
                .userId(userId)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();

        passwordHistoryRepository.save(history);
    }
}
