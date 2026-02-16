package tn.esprit.agroprotect.identity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Filter using Redis.
 * 
 * Implements sliding window rate limiting per IP address.
 * Different limits for different endpoint types.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String LOGIN_RATE_PREFIX = "rate_limit:login:";

    private final StringRedisTemplate redisTemplate;

    @Value("${security.rate-limit.requests-per-minute:100}")
    private int generalRateLimit;

    @Value("${security.rate-limit.login-attempts-per-minute:5}")
    private int loginRateLimit;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String path = request.getServletPath();

        boolean isLoginEndpoint = path.contains("/auth/login") ||
                path.contains("/auth/otp/verify");

        String key;
        int limit;

        if (isLoginEndpoint) {
            key = LOGIN_RATE_PREFIX + clientIp;
            limit = loginRateLimit;
        } else {
            key = RATE_LIMIT_PREFIX + clientIp;
            limit = generalRateLimit;
        }

        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount != null && currentCount == 1) {
                // First request, set expiry
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            // Set rate limit headers
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(Math.max(0, limit - (currentCount != null ? currentCount : 0))));

            if (currentCount != null && currentCount > limit) {
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "error": "TOO_MANY_REQUESTS",
                            "message": "Rate limit exceeded. Please try again later.",
                            "retryAfter": 60
                        }
                        """);
                return;
            }

        } catch (Exception e) {
            // If Redis is unavailable, allow request but log warning
            log.warn("Rate limiting unavailable: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get first IP if multiple
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip rate limiting for documentation and health endpoints
        return path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/actuator/health");
    }
}
