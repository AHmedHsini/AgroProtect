package tn.esprit.agroprotect.identity.service;

import tn.esprit.agroprotect.identity.entity.AuditLog;
import tn.esprit.agroprotect.identity.entity.AuditStatus;
import tn.esprit.agroprotect.identity.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ObjectMapper objectMapper;

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
                    .details(serializeDetails(details))
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
                    .details(serializeDetails(detailsMap))
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save security audit log", e);
        }
    }

    private String serializeDetails(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit details", e);
            return null;
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
