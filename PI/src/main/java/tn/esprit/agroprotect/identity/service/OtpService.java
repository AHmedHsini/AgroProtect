package tn.esprit.agroprotect.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * OTP (One-Time Password) service using Redis for storage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    /**
     * Generate and store OTP for phone number.
     */
    public String generateOtp(String phone, String purpose) {
        String otp = generateNumericOtp();
        String key = buildKey(phone, purpose);

        // Store OTP with expiry
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(expiryMinutes));

        // Reset attempts counter
        String attemptsKey = OTP_ATTEMPTS_PREFIX + key;
        redisTemplate.delete(attemptsKey);

        log.debug("Generated OTP for phone: {} purpose: {}", maskPhone(phone), purpose);
        return otp;
    }

    /**
     * Verify OTP for phone number.
     */
    public OtpVerificationResult verifyOtp(String phone, String purpose, String otp) {
        String key = buildKey(phone, purpose);
        String attemptsKey = OTP_ATTEMPTS_PREFIX + key;

        // Check attempts
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= maxAttempts) {
            // Delete OTP after max attempts
            redisTemplate.delete(key);
            redisTemplate.delete(attemptsKey);
            log.warn("Max OTP attempts exceeded for phone: {}", maskPhone(phone));
            return OtpVerificationResult.maxAttemptsExceeded();
        }

        // Get stored OTP
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            return OtpVerificationResult.expired();
        }

        if (!storedOtp.equals(otp)) {
            // Increment attempts
            redisTemplate.opsForValue().increment(attemptsKey);
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(expiryMinutes));
            log.debug("Invalid OTP attempt for phone: {}", maskPhone(phone));
            return OtpVerificationResult.invalid(maxAttempts - attempts - 1);
        }

        // Success - delete OTP and attempts
        redisTemplate.delete(key);
        redisTemplate.delete(attemptsKey);

        log.info("OTP verified successfully for phone: {}", maskPhone(phone));
        return OtpVerificationResult.successful();
    }

    /**
     * Check if OTP can be requested (rate limiting).
     */
    public boolean canRequestOtp(String phone) {
        String rateLimitKey = "otp_rate:" + phone;
        String count = redisTemplate.opsForValue().get(rateLimitKey);

        if (count != null && Integer.parseInt(count) >= 3) {
            return false; // Max 3 requests per 5 minutes
        }

        Long newCount = redisTemplate.opsForValue().increment(rateLimitKey);
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(rateLimitKey, Duration.ofMinutes(5));
        }

        return true;
    }

    private String generateNumericOtp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private String buildKey(String phone, String purpose) {
        return OTP_PREFIX + purpose + ":" + phone;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4)
            return "****";
        return "****" + phone.substring(phone.length() - 4);
    }

    /**
     * OTP verification result.
     */
    public static class OtpVerificationResult {
        private final boolean success;
        private final boolean expired;
        private final boolean maxAttemptsExceeded;
        private final int remainingAttempts;
        private final String message;

        private OtpVerificationResult(boolean success, boolean expired, boolean maxAttemptsExceeded,
                int remainingAttempts, String message) {
            this.success = success;
            this.expired = expired;
            this.maxAttemptsExceeded = maxAttemptsExceeded;
            this.remainingAttempts = remainingAttempts;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isExpired() {
            return expired;
        }

        public boolean isMaxAttemptsExceeded() {
            return maxAttemptsExceeded;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }

        public String getMessage() {
            return message;
        }

        public static OtpVerificationResult successful() {
            return new OtpVerificationResult(true, false, false, 0, "OTP verified successfully");
        }

        public static OtpVerificationResult invalid(int remaining) {
            return new OtpVerificationResult(false, false, false, remaining,
                    "Invalid OTP. " + remaining + " attempts remaining.");
        }

        public static OtpVerificationResult expired() {
            return new OtpVerificationResult(false, true, false, 0, "OTP has expired");
        }

        public static OtpVerificationResult maxAttemptsExceeded() {
            return new OtpVerificationResult(false, false, true, 0,
                    "Maximum attempts exceeded. Please request a new OTP.");
        }
    }
}
