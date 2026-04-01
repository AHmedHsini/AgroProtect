package AgroProtect.services;

import AgroProtect.entities.AgriculturalCredit;
import AgroProtect.entities.ApplicationStatus;
import AgroProtect.entities.CreditApplication;
import AgroProtect.entities.CreditStatus;
import AgroProtect.repositories.AgriculturalCreditRepository;
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
public class AgriculturalCreditImp implements IAgriculturalCreditService {

    private final AgriculturalCreditRepository creditRepository;
    private final CreditApplicationRepository applicationRepository;

    @Override
    @Transactional
    public AgriculturalCredit createCreditFromApplication(Long applicationId, double approvedAmount) {
        log.info("Creating credit from application: {}, approved amount: {}",
                applicationId, approvedAmount);

        CreditApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new RuntimeException("Only ACCEPTED applications can generate credit. Current: " + app.getStatus());
        }

        if (app.getAgriculturalCredit() != null) {
            throw new RuntimeException("Credit already exists for this application");
        }

        if (approvedAmount <= 0) {
            throw new RuntimeException("Approved amount must be positive");
        }
        if (approvedAmount > app.getRequestedAmount()) {
            throw new RuntimeException(String.format(
                    "Approved amount (%.0f) cannot exceed requested (%.0f)",
                    approvedAmount, app.getRequestedAmount()));
        }

        LoanTerms terms = calculateLoanTerms(app, approvedAmount);

        AgriculturalCredit credit = AgriculturalCredit.builder()
                .approvedAmount(approvedAmount)
                .durationMonths(app.getRequestedDurationMonths())
                .interestRate(terms.getInterestRate())
                .totalRepaymentAmount(terms.getTotalRepayment())
                .gracePeriodMonths(terms.getGracePeriod())
                .disbursementDate(LocalDate.now())
                .maturityDate(LocalDate.now().plusMonths(app.getRequestedDurationMonths()))
                .createdAt(LocalDate.now())
                .status(CreditStatus.ACTIVE)
                .paidAmount(0.0)
                .creditApplication(app)
                .user(app.getUser())
                .build();

        AgriculturalCredit saved = creditRepository.save(credit);

        app.setAgriculturalCredit(saved);
        applicationRepository.save(app);

        log.info("Credit created: ID={}, Amount={:.0f}, Rate={}%, Total={:.0f}, Duration={}mo",
                saved.getIdAgriculturalCredit(),
                saved.getApprovedAmount(),
                String.format("%.2f", saved.getInterestRate() * 100),
                saved.getTotalRepaymentAmount(),
                saved.getDurationMonths());

        return saved;
    }

    private LoanTerms calculateLoanTerms(CreditApplication app, double approvedAmount) {
        double riskScore = app.getRiskScore();

        double baseRate;
        if (riskScore >= 85) baseRate = 0.05;
        else if (riskScore >= 70) baseRate = 0.06;
        else if (riskScore >= 60) baseRate = 0.08;
        else baseRate = 0.10;

        if (approvedAmount > 20000) baseRate += 0.01;

        int duration = app.getRequestedDurationMonths();
        if (duration > 24) baseRate += 0.005;

        int gracePeriod = calculateGracePeriod(app.getPurpose());
        double totalRepayment = calculateTotalRepayment(approvedAmount, baseRate, duration);

        return new LoanTerms(baseRate, gracePeriod, totalRepayment);
    }

    private double calculateTotalRepayment(double principal, double annualRate, int months) {
        if (months <= 12) {
            return principal * (1 + annualRate);
        }
        double years = months / 12.0;
        return principal * Math.pow(1 + annualRate, years);
    }

    private int calculateGracePeriod(String purpose) {
        if (purpose == null) return 1;

        String p = purpose.toLowerCase();
        if (containsAny(p, "tree", "olive", "palm", "orchard", "vineyard")) return 6;
        if (containsAny(p, "equipment", "machinery", "infrastructure", "building")) return 3;
        if (containsAny(p, "livestock", "cattle", "sheep", "poultry")) return 2;
        return 1;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) if (text.contains(k)) return true;
        return false;
    }

    @Override
    @Transactional
    public AgriculturalCredit updateCreditStatus(Long id, CreditStatus newStatus) {
        AgriculturalCredit credit = creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit not found"));

        CreditStatus current = credit.getStatus();

        validateStatusTransition(current, newStatus, credit);

        credit.setStatus(newStatus);

        if (newStatus == CreditStatus.COMPLETED) {
            credit.setClosedAt(LocalDate.now());
        }

        log.info("Credit {} status changed: {} -> {}", id, current, newStatus);
        return creditRepository.save(credit);
    }

    private void validateStatusTransition(CreditStatus current, CreditStatus next, AgriculturalCredit credit) {
        if (current == CreditStatus.COMPLETED ||
                current == CreditStatus.CANCELLED ||
                current == CreditStatus.DEFAULTED) {
            throw new RuntimeException("Credit is in final state: " + current);
        }

        if (next == CreditStatus.COMPLETED) {
            double remaining = credit.getTotalRepaymentAmount() - credit.getPaidAmount();
            if (remaining > 0.01) {
                throw new RuntimeException(String.format(
                        "Cannot complete: not fully paid. Remaining: %.2f TND", remaining));
            }
        }

        if (current == next) {
            throw new RuntimeException("Credit already has status: " + current);
        }
    }

    @Override
    public AgriculturalCredit getAgriculturalCreditById(Long id) {
        return creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit not found"));
    }

    @Override
    public List<AgriculturalCredit> getAllAgriculturalCredit() {
        return (List<AgriculturalCredit>) creditRepository.findAll();
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    private static class LoanTerms {
        private final double interestRate;
        private final int gracePeriod;
        private final double totalRepayment;
    }
}