package AgroProtect.services;

import AgroProtect.entities.BorrowerProfile;
import AgroProtect.entities.CreditApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationService {

    public static class ValidationResult {
        public final boolean valid;
        public final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, "OK");
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * Validates if a loan is affordable for the borrower
     */
    public ValidationResult validateAffordability(CreditApplication application,
                                                  BorrowerProfile profile) {

        double monthlyIncome = profile.getMonthlyIncome();
        double existingDebt = profile.getExistingDebt();
        double requestedAmount = application.getRequestedAmount();
        int duration = application.getRequestedDurationMonths();

        // Check 1: Minimum income
        if (monthlyIncome < 800) {
            return ValidationResult.fail(String.format(
                    "Income %.0f TND below minimum 800 TND", monthlyIncome));
        }

        // Check 2: Maximum loan to income ratio (6x annual)
        double maxLoan = monthlyIncome * 12 * 6;
        if (requestedAmount > maxLoan) {
            return ValidationResult.fail(String.format(
                    "Loan %.0f TND exceeds max %.0f TND (6x annual income)",
                    requestedAmount, maxLoan));
        }

        // Check 3: Monthly payment capacity
        double estimatedMonthly = estimateMonthlyPayment(requestedAmount, duration);
        double maxPayment = monthlyIncome * 0.4; // 40% DTI

        if (estimatedMonthly > maxPayment) {
            int minDuration = calculateMinDuration(requestedAmount, maxPayment);
            return ValidationResult.fail(String.format(
                    "Monthly payment %.0f TND exceeds 40%% of income (%.0f TND). " +
                            "Minimum duration: %d months",
                    estimatedMonthly, maxPayment, minDuration));
        }

        // Check 4: Remaining income for living
        double remaining = monthlyIncome - existingDebt - estimatedMonthly;
        double minLiving = monthlyIncome * 0.5;

        if (remaining < minLiving) {
            return ValidationResult.fail(String.format(
                    "Would leave only %.0f TND for living (need %.0f TND minimum)",
                    remaining, minLiving));
        }

        return ValidationResult.ok();
    }

    private double estimateMonthlyPayment(double amount, int months) {
        double total = amount * 1.08; // 8% interest approximation
        return total / months;
    }

    private int calculateMinDuration(double amount, double maxMonthly) {
        double total = amount * 1.08;
        return (int) Math.ceil(total / maxMonthly);
    }
}