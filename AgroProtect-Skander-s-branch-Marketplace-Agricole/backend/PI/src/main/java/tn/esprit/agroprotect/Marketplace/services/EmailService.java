package tn.esprit.agroprotect.Marketplace.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        // 1. Validate inputs
        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject) || !StringUtils.hasText(body)) {
            log.warn("Email skipped: missing required fields (to, subject, or body)");
            return;
        }

        // 2. Build message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // ✅ Required for Gmail
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        // 3. Send with error handling
        try {
            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
            // Optional: throw custom exception or queue for retry
        }
    }
}