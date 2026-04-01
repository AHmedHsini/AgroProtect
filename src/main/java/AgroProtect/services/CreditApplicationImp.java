package AgroProtect.services;

import AgroProtect.entities.AgriculturalCredit;
import AgroProtect.entities.ApplicationStatus;
import AgroProtect.entities.CreditApplication;
import AgroProtect.repositories.CreditApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditApplicationImp implements ICreditApplicationService {

    private final CreditApplicationRepository crep;
    private final ICreditRiskEngine riskEngine;
    private final IAgriculturalCreditService creditService;
    private final EmailService emailService;

    @Override
    @Transactional
    public CreditApplication addCreditApplication(CreditApplication creditApplication) {
        log.info("Creating new credit application for user: {}",
                creditApplication.getUser() != null ? creditApplication.getUser().getId() : "unknown");

        validateApplication(creditApplication);

        creditApplication.setSubmissionDate(LocalDate.now());
        creditApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
        creditApplication.setEvaluationDate(null);
        creditApplication.setReason(null);
        creditApplication.setRiskScore(0.0);
        creditApplication.setAgriculturalCredit(null);

        CreditApplication saved = crep.save(creditApplication);
        log.info("Application saved with ID: {}", saved.getIdCreditApplication());

        try {
            riskEngine.evaluate(saved);
            saved = crep.save(saved);

            log.info("Evaluation completed: Status={}, Score={:.1f}, Reason={}",
                    saved.getStatus(), saved.getRiskScore(), saved.getReason());

            processDecision(saved);

        } catch (Exception e) {
            log.error("Evaluation failed for application {}: {}",
                    saved.getIdCreditApplication(), e.getMessage(), e);
            saved.setStatus(ApplicationStatus.UNDER_REVIEW);
            saved.setReason("Manual review required: " + e.getMessage());
            saved = crep.save(saved);
        }

        return saved;
    }

    private void validateApplication(CreditApplication app) {
        if (app.getRequestedAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        if (app.getRequestedAmount() > 50000) {
            throw new RuntimeException("Maximum loan amount is 50,000 TND");
        }
        if (app.getRequestedDurationMonths() <= 0 || app.getRequestedDurationMonths() > 60) {
            throw new RuntimeException("Duration must be between 1 and 60 months");
        }
        if (app.getUser() == null) {
            throw new RuntimeException("User is required");
        }
        if (app.getPurpose() == null || app.getPurpose().trim().isEmpty()) {
            throw new RuntimeException("Purpose is required");
        }
        if (app.getPurpose().trim().length() < 10) {
            throw new RuntimeException("Purpose must be at least 10 characters");
        }
    }

    private void processDecision(CreditApplication application) {
        switch (application.getStatus()) {
            case ACCEPTED -> handleAccepted(application);
            case CONDITIONAL -> handleConditional(application);
            case REFUSED -> handleRefused(application);
            default -> log.info("Application {} requires manual review",
                    application.getIdCreditApplication());
        }
    }

    private void handleAccepted(CreditApplication application) {
        log.info("Application {} ACCEPTED - Auto-creating credit",
                application.getIdCreditApplication());

        try {
            double approvedAmount = parseSuggestedAmount(application);
            approvedAmount = Math.min(approvedAmount, application.getRequestedAmount());

            AgriculturalCredit credit = creditService.createCreditFromApplication(
                    application.getIdCreditApplication(),
                    approvedAmount
            );

            sendApplicationAcceptedEmail(application, credit);

        } catch (Exception e) {
            log.error("Failed to auto-create credit for application {}: {}",
                    application.getIdCreditApplication(), e.getMessage());
            application.setStatus(ApplicationStatus.UNDER_REVIEW);
            application.setReason("Auto-credit creation failed: " + e.getMessage());
            crep.save(application);
        }
    }

    private void handleConditional(CreditApplication application) {
        log.info("Application {} CONDITIONAL - Requires manual review",
                application.getIdCreditApplication());
        sendConditionalNotification(application);
    }

    private void handleRefused(CreditApplication application) {
        log.info("Application {} REFUSED", application.getIdCreditApplication());
        sendApplicationRefusedEmail(application);
    }

    private double parseSuggestedAmount(CreditApplication application) {
        String reason = application.getReason();
        if (reason == null) return application.getRequestedAmount();

        try {
            if (reason.contains("Adjusted")) {
                String[] parts = reason.split("to ");
                if (parts.length > 1) {
                    String amountPart = parts[1].split(" ")[0].trim();
                    return Double.parseDouble(amountPart);
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse suggested amount from: {}", reason);
        }

        return application.getRequestedAmount();
    }

    private void sendApplicationAcceptedEmail(CreditApplication application, AgriculturalCredit credit) {
        try {
            String email = application.getUser().getEmail();
            String farmerName = application.getUser().getFullName();

            if (email != null && !email.isEmpty()) {
                emailService.sendApplicationAcceptedEmail(email, farmerName, application, credit);
                log.info("Acceptance email sent to: {}", email);
            }
        } catch (Exception e) {
            log.error("Failed to send acceptance email: {}", e.getMessage());
        }
    }

    private void sendApplicationRefusedEmail(CreditApplication application) {
        try {
            String email = application.getUser().getEmail();
            String farmerName = application.getUser().getFullName();

            if (email != null && !email.isEmpty()) {
                String reason = application.getReason();
                emailService.sendApplicationRefusedEmail(email, farmerName, application, reason);
                log.info("Refusal email sent to: {}", email);
            }
        } catch (Exception e) {
            log.error("Failed to send refusal email: {}", e.getMessage());
        }
    }

    private void sendConditionalNotification(CreditApplication application) {
        try {
            String email = application.getUser().getEmail();
            if (email != null && !email.isEmpty()) {
                log.info("Conditional notification would be sent to: {}", email);
            }
        } catch (Exception e) {
            log.error("Failed to send conditional notification: {}", e.getMessage());
        }
    }

    @Override
    public void DeleteCreditApplication(long idCreditApplication) {
        CreditApplication app = crep.findById(idCreditApplication)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getAgriculturalCredit() != null) {
            throw new RuntimeException("Cannot delete: Credit already created");
        }

        crep.deleteById(idCreditApplication);
        log.info("Deleted application: {}", idCreditApplication);
    }

    @Override
    public CreditApplication getCreditApplicationById(long idCreditApplication) {
        return crep.findById(idCreditApplication)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    @Override
    public List<CreditApplication> getAllCreditApplication() {
        return (List<CreditApplication>) crep.findAll();
    }

    @Override
    public List<CreditApplication> addAllCreditApplication(List<CreditApplication> creditApplications) {
        return creditApplications.stream()
                .map(this::addCreditApplication)
                .toList();
    }

    @Transactional
    public CreditApplication reEvaluate(Long id) {
        CreditApplication app = crep.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getAgriculturalCredit() != null) {
            throw new RuntimeException("Cannot re-evaluate: Credit already created");
        }

        log.info("Manual re-evaluation triggered for application: {}", id);

        app.setStatus(ApplicationStatus.UNDER_REVIEW);
        riskEngine.evaluate(app);

        processDecision(app);

        return crep.save(app);
    }
}