package AgroProtect.services;

import AgroProtect.DTOs.AIEnhancedRiskResultDTO;
import AgroProtect.entities.*;
import AgroProtect.repositories.BorrowerProfileRepository;
import AgroProtect.repositories.CreditApplicationRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditRiskEngine implements ICreditRiskEngine {

    private final CreditApplicationRepository creditApplicationRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;
    private final CreditRiskMLService mlService;

    // ========== CONFIGURATION CONSTANTS ==========

    private static final double BASE_SCORE = 100.0;
    private static final double MIN_ACCEPTABLE_SCORE = 60.0;
    private static final double CONDITIONAL_THRESHOLD = 75.0;
    private static final double EXCELLENT_THRESHOLD = 85.0;

    private static final double MAX_DTI_RATIO = 0.40;
    private static final double MAX_LOAN_TO_INCOME_RATIO = 6.0;
    private static final double MIN_INCOME_THRESHOLD = 800.0;

    private static final double LOW_RISK_AMOUNT = 5000.0;
    private static final double MEDIUM_RISK_AMOUNT = 15000.0;
    private static final double HIGH_RISK_AMOUNT = 30000.0;

    // ========== MAIN EVALUATION METHOD ==========

    @Override
    @Transactional
    public void evaluate(CreditApplication application) {
        log.info("Starting comprehensive risk evaluation for application ID: {}",
                application.getIdCreditApplication());

        BorrowerProfile profile = borrowerProfileRepository.findByUser(application.getUser())
                .orElseThrow(() -> new RuntimeException("Borrower profile not found for user: " +
                        application.getUser().getId()));

        // STEP 1: Calculate rule-based risk score
        RiskAssessment ruleBasedAssessment = calculateRiskScore(application, profile);
        double ruleBasedScore = ruleBasedAssessment.getFinalScore();

        // STEP 2: Get ML prediction
        CreditRiskMLService.MLPrediction mlPrediction =
                mlService.predictDefaultProbability(profile, application);

        // STEP 3: Calculate AI-suggested optimal terms
        LoanTermsSuggestion aiSuggestion = calculateOptimalTerms(
                application, profile, ruleBasedScore, mlPrediction);

        // STEP 4: AFFORDABILITY CHECK
        AffordabilityCheck affordability = checkAffordability(application, profile, aiSuggestion);

        // STEP 5: Combine all factors for final decision
        EvaluationResult result = makeFinalDecision(
                application, ruleBasedAssessment, mlPrediction,
                aiSuggestion, affordability, profile);

        // STEP 6: Apply decision
        application.setRiskScore(result.getFinalScore());
        application.setEvaluationDate(LocalDate.now());
        application.setStatus(result.getStatus());
        application.setReason(result.getReason());

        log.info("Evaluation complete: Status={}, Score={:.1f}, Suggested={:.0f} TND, Reason={}",
                result.getStatus(), result.getFinalScore(),
                result.getSuggestedAmount(), result.getReason());
    }

    // ========== STEP 1: RULE-BASED RISK ASSESSMENT ==========

    private RiskAssessment calculateRiskScore(CreditApplication application, BorrowerProfile profile) {
        RiskAssessment assessment = new RiskAssessment();
        double score = BASE_SCORE;

        double incomeScore = assessIncomeStability(profile);
        score += incomeScore;
        assessment.setIncomeScore(incomeScore);

        double debtScore = assessDebtBurden(profile, application);
        score += debtScore;
        assessment.setDebtScore(debtScore);

        double historyScore = assessCreditHistory(profile);
        score += historyScore;
        assessment.setHistoryScore(historyScore);

        double amountScore = assessAmountRisk(application);
        score += amountScore;
        assessment.setAmountScore(amountScore);

        double projectScore = assessProjectViability(application);
        score += projectScore;
        assessment.setProjectScore(projectScore);

        double capacityScore = assessRepaymentCapacity(profile, application);
        score += capacityScore;
        assessment.setCapacityScore(capacityScore);

        double finalScore = Math.max(0, Math.min(100, score));
        assessment.setFinalScore(finalScore);
        assessment.setRiskLevel(calculateRiskLevel(finalScore));
        assessment.setRiskFactors(extractRiskFactors(assessment));

        return assessment;
    }

    private double assessIncomeStability(BorrowerProfile profile) {
        double monthlyIncome = profile.getMonthlyIncome();

        if (monthlyIncome < MIN_INCOME_THRESHOLD) {
            return -40;
        }

        if (monthlyIncome >= 5000) return 25;
        if (monthlyIncome >= 3500) return 20;
        if (monthlyIncome >= 2500) return 15;
        if (monthlyIncome >= 1500) return 10;
        if (monthlyIncome >= 1000) return 5;
        return 0;
    }

    private double assessDebtBurden(BorrowerProfile profile, CreditApplication application) {
        double monthlyIncome = profile.getMonthlyIncome();
        double existingDebt = profile.getExistingDebt();

        if (monthlyIncome <= 0) return -50;

        double currentDTI = existingDebt / monthlyIncome;
        double estimatedNewPayment = estimateMonthlyPayment(application);
        double projectedDTI = (existingDebt + estimatedNewPayment) / monthlyIncome;

        double score = 0;

        if (currentDTI < 0.20) score += 15;
        else if (currentDTI < 0.30) score += 10;
        else if (currentDTI < 0.40) score += 5;
        else if (currentDTI < 0.50) score -= 10;
        else score -= 25;

        if (projectedDTI < 0.30) score += 15;
        else if (projectedDTI < 0.40) score += 10;
        else if (projectedDTI < 0.50) score -= 5;
        else if (projectedDTI < 0.60) score -= 20;
        else score -= 40;

        return score;
    }

    private double assessCreditHistory(BorrowerProfile profile) {
        int completed = profile.getPreviousCompletedCredits();
        int defaulted = profile.getPreviousDefaultedCredits();

        if (defaulted > 0) {
            double penalty = -25 * defaulted;
            if (defaulted >= 2) penalty -= 30;
            if (defaulted >= 3) penalty -= 50;
            return penalty;
        }

        if (completed >= 5) return 30;
        if (completed >= 3) return 25;
        if (completed >= 1) return 15;
        return 5;
    }

    private double assessAmountRisk(CreditApplication application) {
        double amount = application.getRequestedAmount();
        int duration = application.getRequestedDurationMonths();

        double score = 0;

        if (amount <= LOW_RISK_AMOUNT) score = 10;
        else if (amount <= MEDIUM_RISK_AMOUNT) score = 5;
        else if (amount <= HIGH_RISK_AMOUNT) score = -5;
        else score = -15;

        if (duration > 36) score -= 10;
        else if (duration > 24) score -= 5;
        else if (duration <= 6) score += 5;

        double monthlyPrincipal = amount / duration;
        if (monthlyPrincipal > 2000) score -= 10;
        if (monthlyPrincipal > 5000) score -= 15;

        return score;
    }

    private double assessProjectViability(CreditApplication application) {
        String purpose = application.getPurpose();

        if (purpose == null || purpose.trim().isEmpty()) {
            return -30;
        }

        String trimmed = purpose.trim();
        if (trimmed.length() < 10) {
            return -25;
        }

        String[] words = trimmed.split("\\s+");
        if (words.length < 3) {
            return -20;
        }

        String purposeLower = trimmed.toLowerCase();

        if (containsAny(purposeLower, "irrigation", "equipment", "machinery",
                "infrastructure", "greenhouse", "solar", "storage")) {
            return 15;
        }

        if (containsAny(purposeLower, "seeds", "fertilizer", "livestock",
                "feed", "veterinary", "crop", "planting")) {
            return 10;
        }

        if (containsAny(purposeLower, "expansion", "new crop", "diversification",
                "organic", "export")) {
            return 5;
        }

        if (containsAny(purposeLower, "personal", "debt", "consumption",
                "medical", "wedding", "travel")) {
            return -20;
        }

        return 0;
    }

    private double assessRepaymentCapacity(BorrowerProfile profile, CreditApplication application) {
        double monthlyIncome = profile.getMonthlyIncome();
        double existingDebt = profile.getExistingDebt();
        double estimatedPayment = estimateMonthlyPayment(application);

        double remainingIncome = monthlyIncome - existingDebt - estimatedPayment;
        double remainingRatio = remainingIncome / monthlyIncome;

        if (remainingIncome <= 0) {
            return -50;
        }

        if (remainingRatio < 0.30) {
            return -30;
        }

        if (remainingRatio >= 0.60) return 15;
        if (remainingRatio >= 0.50) return 10;
        if (remainingRatio >= 0.40) return 5;
        if (remainingRatio >= 0.30) return 0;
        return -10;
    }

    // ========== STEP 3: OPTIMAL TERMS CALCULATION ==========

    private LoanTermsSuggestion calculateOptimalTerms(CreditApplication application,
                                                      BorrowerProfile profile, double riskScore,
                                                      CreditRiskMLService.MLPrediction mlPrediction) {

        double requestedAmount = application.getRequestedAmount();
        int requestedDuration = application.getRequestedDurationMonths();

        double suggestedAmount = requestedAmount;
        int suggestedDuration = requestedDuration;
        double suggestedRate;

        if (riskScore >= EXCELLENT_THRESHOLD) {
            suggestedRate = 0.05;
        } else if (riskScore >= CONDITIONAL_THRESHOLD) {
            suggestedRate = 0.06;
        } else if (riskScore >= MIN_ACCEPTABLE_SCORE) {
            suggestedRate = 0.08;
        } else {
            suggestedRate = 0.10;
        }

        if (mlPrediction.isModelAvailable() && mlPrediction.getDefaultProbability() > 0.3) {
            suggestedRate = Math.min(suggestedRate + 0.02, 0.12);
            if (mlPrediction.getDefaultProbability() > 0.5) {
                suggestedAmount *= 0.8;
            }
        }

        double maxMonthlyPayment = profile.getMonthlyIncome() * MAX_DTI_RATIO;
        double affordableAmount = calculateMaxAffordableAmount(
                maxMonthlyPayment, suggestedDuration, suggestedRate);

        double maxByAnnualIncome = profile.getMonthlyIncome() * 12 * MAX_LOAN_TO_INCOME_RATIO;
        affordableAmount = Math.min(affordableAmount, maxByAnnualIncome);

        if (suggestedAmount > affordableAmount) {
            suggestedAmount = affordableAmount;

            if (requestedDuration < 60) {
                int extendedDuration = Math.min(requestedDuration * 2, 60);
                double amountWithExtension = calculateMaxAffordableAmount(
                        maxMonthlyPayment, extendedDuration, suggestedRate);

                if (amountWithExtension >= requestedAmount * 0.9) {
                    suggestedDuration = extendedDuration;
                    suggestedAmount = requestedAmount;
                } else if (amountWithExtension > suggestedAmount) {
                    suggestedDuration = extendedDuration;
                    suggestedAmount = amountWithExtension;
                }
            }
        }

        suggestedAmount = Math.floor(suggestedAmount / 100) * 100;

        return new LoanTermsSuggestion(suggestedAmount, suggestedDuration, suggestedRate,
                affordableAmount, requestedAmount, requestedDuration);
    }

    // ========== STEP 4: AFFORDABILITY CHECK ==========

    private AffordabilityCheck checkAffordability(CreditApplication application,
                                                  BorrowerProfile profile, LoanTermsSuggestion suggestion) {

        double monthlyIncome = profile.getMonthlyIncome();
        double existingDebt = profile.getExistingDebt();
        double requestedAmount = application.getRequestedAmount();

        if (monthlyIncome < MIN_INCOME_THRESHOLD) {
            return new AffordabilityCheck(false, false, 0, 0,
                    String.format("Income (%.0f TND) below minimum (%.0f TND)",
                            monthlyIncome, MIN_INCOME_THRESHOLD), "CRITICAL");
        }

        double maxLoanAmount = monthlyIncome * 12 * MAX_LOAN_TO_INCOME_RATIO;
        if (requestedAmount > maxLoanAmount) {
            return new AffordabilityCheck(false, true, maxLoanAmount,
                    application.getRequestedDurationMonths(),
                    String.format("Requested (%.0f) exceeds max (%.0f = %dx annual income)",
                            requestedAmount, maxLoanAmount, (int)MAX_LOAN_TO_INCOME_RATIO),
                    "HIGH");
        }

        double maxMonthlyPayment = monthlyIncome * MAX_DTI_RATIO;
        double estimatedMonthlyPayment = estimateMonthlyPayment(application);

        if (estimatedMonthlyPayment > maxMonthlyPayment) {
            int minDurationNeeded = calculateMinDurationForAffordability(
                    requestedAmount, maxMonthlyPayment, suggestion.getSuggestedRate());

            return new AffordabilityCheck(false, true, requestedAmount,
                    Math.max(minDurationNeeded, application.getRequestedDurationMonths()),
                    String.format("Payment (%.0f) exceeds 40%% income (%.0f). Min duration: %d months",
                            estimatedMonthlyPayment, maxMonthlyPayment, minDurationNeeded),
                    "HIGH");
        }

        double remainingIncome = monthlyIncome - existingDebt - estimatedMonthlyPayment;
        double minimumLivingStandard = monthlyIncome * 0.50;

        if (remainingIncome < minimumLivingStandard) {
            return new AffordabilityCheck(false, true, suggestion.getSuggestedAmount(),
                    suggestion.getSuggestedDuration(),
                    String.format("Leaves only %.0f TND for living (need %.0f TND)",
                            remainingIncome, minimumLivingStandard),
                    "MEDIUM");
        }

        return new AffordabilityCheck(true, true, requestedAmount,
                application.getRequestedDurationMonths(),
                "Affordable within income constraints", "NONE");
    }

    // ========== STEP 5: FINAL DECISION ==========

    private EvaluationResult makeFinalDecision(CreditApplication application,
                                               RiskAssessment ruleBased, CreditRiskMLService.MLPrediction mlPrediction,
                                               LoanTermsSuggestion aiSuggestion, AffordabilityCheck affordability,
                                               BorrowerProfile profile) {

        double combinedScore = combineScores(ruleBased.getFinalScore(), mlPrediction);
        double requestedAmount = application.getRequestedAmount();
        int requestedDuration = application.getRequestedDurationMonths();

        // Determine base status from score (BEFORE affordability check)
        ApplicationStatus baseStatus;
        if (mlPrediction.isModelAvailable() && mlPrediction.getDefaultProbability() > 0.5) {
            baseStatus = ApplicationStatus.REFUSED;
        } else if (combinedScore >= CONDITIONAL_THRESHOLD) {
            baseStatus = ApplicationStatus.ACCEPTED;
        } else if (combinedScore >= MIN_ACCEPTABLE_SCORE) {
            baseStatus = ApplicationStatus.CONDITIONAL;
        } else {
            baseStatus = ApplicationStatus.REFUSED;
        }

        // AFFORDABILITY VETO: Check if loan is fundamentally unaffordable
        if (!affordability.isAffordable()) {

            // CRITICAL: Cannot be made affordable (e.g., income too low)
            if (affordability.getSeverity().equals("CRITICAL")) {
                return new EvaluationResult(ApplicationStatus.REFUSED,
                        combinedScore * 0.5, 0, 0, 0,
                        "REFUSED: " + affordability.getReason());
            }

            // HIGH/MEDIUM: Can be affordable with adjusted terms
            // Calculate what the score would be with adjusted terms
            double adjustedScore = combinedScore;

            // If we need to reduce amount significantly, penalize score
            double reductionRatio = affordability.getSuggestedAmount() / requestedAmount;
            if (reductionRatio < 0.7) {
                adjustedScore -= 20; // Significant penalty for large reduction
            } else if (reductionRatio < 0.9) {
                adjustedScore -= 10; // Minor penalty
            }

            // Re-evaluate status based on adjusted score
            ApplicationStatus adjustedStatus;
            if (adjustedScore >= CONDITIONAL_THRESHOLD) {
                adjustedStatus = ApplicationStatus.CONDITIONAL; // Could accept with terms
            } else if (adjustedScore >= MIN_ACCEPTABLE_SCORE) {
                adjustedStatus = ApplicationStatus.CONDITIONAL;
            } else {
                adjustedStatus = ApplicationStatus.REFUSED; // Score too low even with adjustments
            }

            // Build appropriate message based on final status
            String prefix = adjustedStatus == ApplicationStatus.REFUSED ? "REFUSED" : "CONDITIONAL";

            return new EvaluationResult(adjustedStatus, adjustedScore,
                    affordability.getSuggestedAmount(),
                    affordability.getSuggestedDuration(),
                    aiSuggestion.getSuggestedRate(),
                    prefix + ": " + affordability.getReason() +
                            " | Suggested terms: " + String.format("%.0f TND over %d months",
                            affordability.getSuggestedAmount(), affordability.getSuggestedDuration()));
        }

        // Affordability passed - check if AI suggests different terms than requested
        double finalAmount = requestedAmount;
        int finalDuration = requestedDuration;
        boolean termsAdjusted = false;

        if (aiSuggestion.getSuggestedAmount() < requestedAmount * 0.95) {
            finalAmount = aiSuggestion.getSuggestedAmount();
            finalDuration = aiSuggestion.getSuggestedDuration();
            termsAdjusted = true;
        } else if (aiSuggestion.getSuggestedDuration() != requestedDuration) {
            finalDuration = aiSuggestion.getSuggestedDuration();
            termsAdjusted = true;
        }

        // If terms adjusted and was ACCEPTED, consider downgrading to CONDITIONAL
        if (termsAdjusted && baseStatus == ApplicationStatus.ACCEPTED &&
                finalAmount < requestedAmount * 0.90) {
            baseStatus = ApplicationStatus.CONDITIONAL;
        }

        // Build final reason string
        StringBuilder reason = new StringBuilder();
        reason.append(String.format("Score: %.1f/100. ", combinedScore));

        if (mlPrediction.isModelAvailable()) {
            reason.append(String.format("ML Risk: %.1f%% (%s). ",
                    mlPrediction.getDefaultProbability() * 100,
                    mlPrediction.getRiskCategory()));
        }

        if (termsAdjusted) {
            reason.append(String.format("Adjusted %.0f/%dmo to %.0f/%dmo. ",
                    requestedAmount, requestedDuration, finalAmount, finalDuration));
        } else {
            reason.append("Terms approved as requested. ");
        }

        if (!ruleBased.getRiskFactors().isEmpty()) {
            reason.append("Risks: " + String.join(", ", ruleBased.getRiskFactors()));
        }

        return new EvaluationResult(baseStatus, combinedScore, finalAmount,
                finalDuration, aiSuggestion.getSuggestedRate(), reason.toString());
    }

    // ========== HELPER METHODS ==========

    private double combineScores(double ruleBasedScore, CreditRiskMLService.MLPrediction mlPrediction) {
        if (!mlPrediction.isModelAvailable()) {
            return ruleBasedScore;
        }

        double mlScore = (1 - mlPrediction.getDefaultProbability()) * 100;
        double confidenceWeight = mlPrediction.getConfidence();

        return ruleBasedScore * (1 - confidenceWeight * 0.4) + mlScore * (confidenceWeight * 0.4);
    }

    private double estimateMonthlyPayment(CreditApplication application) {
        double amount = application.getRequestedAmount();
        int months = application.getRequestedDurationMonths();
        double total = amount * 1.08;
        return total / months;
    }

    private double calculateMaxAffordableAmount(double maxMonthlyPayment, int duration, double rate) {
        if (rate == 0) return maxMonthlyPayment * duration;

        double monthlyRate = rate / 12;
        double factor = (Math.pow(1 + monthlyRate, duration) - 1) /
                (monthlyRate * Math.pow(1 + monthlyRate, duration));
        return maxMonthlyPayment * factor;
    }

    private int calculateMinDurationForAffordability(double amount, double maxMonthlyPayment, double rate) {
        int months = 1;
        while (months <= 60) {
            double monthly = calculateMonthlyPayment(amount, months, rate);
            if (monthly <= maxMonthlyPayment) {
                return months;
            }
            months++;
        }
        return 60;
    }

    private double calculateMonthlyPayment(double amount, int months, double rate) {
        double monthlyRate = rate / 12;
        if (monthlyRate == 0) return amount / months;
        return amount * (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);
    }

    private RiskLevel calculateRiskLevel(double score) {
        if (score >= 80) return RiskLevel.LOW;
        if (score >= 60) return RiskLevel.MEDIUM;
        if (score >= 40) return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }

    private List<String> extractRiskFactors(RiskAssessment assessment) {
        List<String> factors = new ArrayList<>();
        if (assessment.getIncomeScore() < 10) factors.add("Low income");
        if (assessment.getDebtScore() < 10) factors.add("High debt");
        if (assessment.getHistoryScore() < 0) factors.add("Previous defaults");
        if (assessment.getAmountScore() < 0) factors.add("High loan amount");
        if (assessment.getProjectScore() < 0) factors.add("Weak project");
        if (assessment.getCapacityScore() < 0) factors.add("Tight capacity");
        return factors;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    // ========== PUBLIC REPORT METHOD ==========

    public AIEnhancedRiskResultDTO getDetailedRiskReport(Long applicationId) {
        CreditApplication app = creditApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        BorrowerProfile profile = borrowerProfileRepository.findByUser(app.getUser())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        RiskAssessment ruleBased = calculateRiskScore(app, profile);
        CreditRiskMLService.MLPrediction mlPred = mlService.predictDefaultProbability(profile, app);
        LoanTermsSuggestion suggestion = calculateOptimalTerms(app, profile,
                ruleBased.getFinalScore(), mlPred);
        AffordabilityCheck affordability = checkAffordability(app, profile, suggestion);

        String reasoning = affordability.isAffordable() ?
                (suggestion.getSuggestedAmount() < app.getRequestedAmount() ?
                        "Risk-based reduction" : "Terms optimal") :
                "AFFORDABILITY: " + affordability.getReason();

        return AIEnhancedRiskResultDTO.builder()
                .applicationId(applicationId)
                .ruleBasedScore(ruleBased.getFinalScore())
                .mlDefaultProbability(mlPred.getDefaultProbability())
                .mlConfidence(mlPred.getConfidence())
                .mlAvailable(mlPred.isModelAvailable())
                .combinedScore(combineScores(ruleBased.getFinalScore(), mlPred))
                .finalDecision(app.getStatus().toString())
                .aiSuggestedAmount(suggestion.getSuggestedAmount())
                .aiSuggestedDuration(suggestion.getSuggestedDuration())
                .aiSuggestedRate(suggestion.getSuggestedRate())
                .aiReasoning(reasoning)
                .similarCases(mlService.findSimilarCases(profile, app))
                .riskFactors(ruleBased.getRiskFactors())
                .build();
    }

    // ========== INNER CLASSES WITH PROPER CONSTRUCTORS ==========

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RiskAssessment {
        private double finalScore;
        private double incomeScore;
        private double debtScore;
        private double historyScore;
        private double amountScore;
        private double projectScore;
        private double capacityScore;
        private RiskLevel riskLevel;
        private List<String> riskFactors = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LoanTermsSuggestion {
        private double suggestedAmount;
        private int suggestedDuration;
        private double suggestedRate;
        private double maxAffordableAmount;
        private double requestedAmount;
        private int requestedDuration;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AffordabilityCheck {
        private boolean isAffordable;
        private boolean canBeAffordableWithTerms;
        private double suggestedAmount;
        private int suggestedDuration;
        private String reason;
        private String severity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class EvaluationResult {
        private ApplicationStatus status;
        private double finalScore;
        private double suggestedAmount;
        private int suggestedDuration;
        private double suggestedRate;
        private String reason;
    }

    private enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}