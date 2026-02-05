package com.agriplatform.identity.service;

import com.agriplatform.identity.entity.AuditLog;
import com.agriplatform.identity.entity.AuditStatus;
import com.agriplatform.identity.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Audit logging service for security events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an authentication event.
     */
    @Async
    public void logAuthEvent(Long userId, String action, AuditStatus status,
            String failureReason, HttpServletRequest request) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .actorId(userId)
                    .action(action)
                    .resourceType("AUTH")
                    .status(status)
                    .failureReason(failureReason)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .deviceId(request.getHeader("X-Device-Id"))
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Audit log: action={}, status={}, userId={}", action, status, userId);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    /**
     * Log a user action.
     */
    @Async
    public void logUserAction(Long userId, Long actorId, String action,
            String resourceType, String resourceId,
            AuditStatus status, Map<String, Object> details,
            HttpServletRequest request) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .actorId(actorId)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .status(status)
                    .details(details)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .deviceId(request.getHeader("X-Device-Id"))
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    /**
     * Log a security event with custom details.
     */
    @Async
    public void logSecurityEvent(String action, String details, HttpServletRequest request) {
        try {
            Map<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("message", details);

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .resourceType("SECURITY")
                    .status(AuditStatus.SUCCESS)
                    .details(detailsMap)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save security audit log", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null)
            return null;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
