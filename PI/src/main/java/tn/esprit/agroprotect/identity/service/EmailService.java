package tn.esprit.agroprotect.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email service for sending verification and notification emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@agriplatform.com}")
    private String fromEmail;

    @Value("${spring.application.name:AgriPlatform}")
    private String applicationName;

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Send email verification link.
     */
    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = frontendUrl + "/auth/verify-email?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(applicationName + " - Verify Your Email");
            message.setText(String.format("""
                    Welcome to %s!

                    Please verify your email address by clicking the link below:

                    %s

                    This link will expire in 24 hours.

                    If you did not create an account, please ignore this email.

                    Best regards,
                    The %s Team
                    """, applicationName, verificationUrl, applicationName));

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    /**
     * Send password reset link.
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(applicationName + " - Password Reset Request");
            message.setText(String.format(
                    """
                            Hello,

                            We received a request to reset your password. Click the link below to set a new password:

                            %s

                            This link will expire in 1 hour.

                            If you did not request a password reset, please ignore this email or contact support if you have concerns.

                            Best regards,
                            The %s Team
                            """,
                    resetUrl, applicationName));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    /**
     * Send security alert email.
     */
    @Async
    public void sendSecurityAlertEmail(String toEmail, String alertType, String details) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(applicationName + " - Security Alert: " + alertType);
            message.setText(String.format(
                    """
                            Hello,

                            We detected unusual activity on your account:

                            %s

                            If this was you, no action is needed. If you don't recognize this activity, please secure your account immediately by changing your password.

                            Best regards,
                            The %s Team
                            """,
                    details, applicationName));

            mailSender.send(message);
            log.info("Security alert email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send security alert email to: {}", toEmail, e);
        }
    }
}
