package AgroProtect.services;

import AgroProtect.entities.*;
import AgroProtect.repositories.AgriculturalCreditRepository;
import AgroProtect.repositories.BorrowerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import weka.classifiers.functions.Logistic;
import weka.core.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditRiskMLService {

    private final AgriculturalCreditRepository creditRepository;
    private final BorrowerProfileRepository borrowerProfileRepository;

    private Logistic classifier;
    private Instances datasetStructure;
    private boolean modelTrained = false;

    /**
     * Train ML model on historical repayment data
     */
    public void trainModel() {
        try {
            List<AgriculturalCredit> historicalCredits = (List<AgriculturalCredit>) creditRepository.findAll();

            if (historicalCredits.size() < 10) {
                log.warn("Insufficient data for ML training ({} records). Using rule-based only.", historicalCredits.size());
                return;
            }

            // Create dataset structure
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("monthlyIncome"));
            attributes.add(new Attribute("existingDebt"));
            attributes.add(new Attribute("dtiRatio"));
            attributes.add(new Attribute("previousCompleted"));
            attributes.add(new Attribute("previousDefaulted"));
            attributes.add(new Attribute("requestedAmount"));
            attributes.add(new Attribute("durationMonths"));
            attributes.add(new Attribute("yearsExperience"));
            attributes.add(new Attribute("landArea"));

            // Class attribute (0 = repaid, 1 = defaulted)
            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("repaid");
            classValues.add("defaulted");
            attributes.add(new Attribute("outcome", classValues));

            datasetStructure = new Instances("CreditRisk", attributes, 0);
            datasetStructure.setClassIndex(attributes.size() - 1);

            // Add training instances
            int validRecords = 0;
            for (AgriculturalCredit credit : historicalCredits) {
                if ((credit.getStatus() == CreditStatus.COMPLETED ||
                        credit.getStatus() == CreditStatus.DEFAULTED)) {

                    // Get profile through repository
                    BorrowerProfile profile = borrowerProfileRepository.findByUser(credit.getCreditApplication().getUser())
                            .orElse(null);

                    if (profile == null) continue;

                    double[] values = extractFeatures(profile, credit.getCreditApplication(), credit);
                    Instance instance = new DenseInstance(1.0, values);
                    instance.setDataset(datasetStructure);

                    // Set class value
                    String outcome = credit.getStatus() == CreditStatus.DEFAULTED ? "defaulted" : "repaid";
                    instance.setClassValue(outcome);

                    datasetStructure.add(instance);
                    validRecords++;
                }
            }

            if (validRecords < 5) {
                log.warn("Insufficient completed/defaulted credits for training ({} valid)", validRecords);
                return;
            }

            // Train logistic regression classifier
            classifier = new Logistic();
            classifier.buildClassifier(datasetStructure);
            modelTrained = true;

            log.info("✅ ML Model trained successfully on {} instances", validRecords);

        } catch (Exception e) {
            log.error("Failed to train ML model: {}", e.getMessage());
            modelTrained = false;
        }
    }

    /**
     * Predict default probability using ML
     */
    public MLPrediction predictDefaultProbability(BorrowerProfile profile,
                                                  CreditApplication application) {
        if (!modelTrained) {
            return MLPrediction.builder()
                    .modelAvailable(false)
                    .defaultProbability(0.5)
                    .confidence(0.0)
                    .build();
        }

        try {
            double[] features = extractFeatures(profile, application, null);
            Instance instance = new DenseInstance(1.0, features);
            instance.setDataset(datasetStructure);

            // Get probability distribution [repaid, defaulted]
            double[] distribution = classifier.distributionForInstance(instance);

            double defaultProb = distribution[1];
            double confidence = Math.abs(distribution[0] - distribution[1]);

            return MLPrediction.builder()
                    .modelAvailable(true)
                    .defaultProbability(defaultProb)
                    .confidence(confidence)
                    .riskCategory(categorizeRisk(defaultProb))
                    .build();

        } catch (Exception e) {
            log.error("ML prediction failed: {}", e.getMessage());
            return MLPrediction.builder()
                    .modelAvailable(false)
                    .defaultProbability(0.5)
                    .confidence(0.0)
                    .build();
        }
    }

    /**
     * Suggest optimal loan terms using AI
     */
    public LoanSuggestion suggestOptimalTerms(BorrowerProfile profile,
                                              CreditApplication application,
                                              double currentRiskScore) {

        double requestedAmount = application.getRequestedAmount();
        int requestedDuration = application.getRequestedDurationMonths();

        double suggestedAmount = requestedAmount;
        int suggestedDuration = requestedDuration;
        double suggestedRate = 0.08;

        // Risk-based adjustments
        if (currentRiskScore >= 85) {
            suggestedAmount = Math.min(requestedAmount * 1.1, 50000);
            suggestedRate = 0.05;
        } else if (currentRiskScore >= 70) {
            suggestedAmount = requestedAmount;
            suggestedRate = 0.06;
        } else if (currentRiskScore >= 60) {
            suggestedAmount = requestedAmount * 0.9;
            suggestedDuration = Math.min(requestedDuration + 6, 60);
            suggestedRate = 0.08;
        } else {
            suggestedAmount = requestedAmount * 0.75;
            suggestedDuration = Math.min(requestedDuration + 12, 60);
            suggestedRate = 0.10;
        }

        // Income-based cap
        double maxAffordable = profile.getMonthlyIncome() * 0.4 * suggestedDuration;
        if (suggestedAmount > maxAffordable) {
            suggestedAmount = maxAffordable;
        }

        return LoanSuggestion.builder()
                .suggestedAmount(Math.round(suggestedAmount))
                .suggestedDuration(suggestedDuration)
                .suggestedInterestRate(suggestedRate)
                .suggestedMonthlyPayment(calculateMonthlyPayment(suggestedAmount, suggestedDuration, suggestedRate))
                .reasoning(generateSuggestionReason(currentRiskScore, requestedAmount, suggestedAmount))
                .build();
    }

    /**
     * Find similar farmers and their outcomes
     */
    public List<SimilarFarmerCase> findSimilarCases(BorrowerProfile profile,
                                                    CreditApplication application) {
        List<SimilarFarmerCase> similarCases = new ArrayList<>();

        List<AgriculturalCredit> allCredits = (List<AgriculturalCredit>) creditRepository.findAll();

        for (AgriculturalCredit credit : allCredits) {
            if (credit.getStatus() == CreditStatus.COMPLETED ||
                    credit.getStatus() == CreditStatus.DEFAULTED) {

                // Get other profile through repository
                BorrowerProfile otherProfile = borrowerProfileRepository.findByUser(credit.getCreditApplication().getUser())
                        .orElse(null);

                if (otherProfile == null) continue;

                double similarity = calculateSimilarity(profile, application, otherProfile, credit);

                if (similarity > 0.7) {
                    similarCases.add(SimilarFarmerCase.builder()
                            .similarityScore(similarity)
                            .amount(credit.getApprovedAmount())
                            .duration(credit.getDurationMonths())
                            .outcome(credit.getStatus().toString())
                            .build());
                }
            }
        }

        similarCases.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

        return similarCases.subList(0, Math.min(5, similarCases.size()));
    }

    // ===== HELPER METHODS =====

    private double[] extractFeatures(BorrowerProfile profile,
                                     CreditApplication application,
                                     AgriculturalCredit credit) {
        double amount = credit != null ? credit.getApprovedAmount() : application.getRequestedAmount();
        int duration = credit != null ? credit.getDurationMonths() : application.getRequestedDurationMonths();

        return new double[] {
                profile.getMonthlyIncome(),
                profile.getExistingDebt(),
                profile.getDebtToIncomeRatio(),
                profile.getPreviousCompletedCredits(),
                profile.getPreviousDefaultedCredits(),
                amount,
                duration,
                profile.getYearsOfExperience(),
                profile.getTotalLandArea(),
                0
        };
    }

    private String categorizeRisk(double defaultProbability) {
        if (defaultProbability < 0.1) return "VERY_LOW";
        if (defaultProbability < 0.25) return "LOW";
        if (defaultProbability < 0.4) return "MEDIUM";
        if (defaultProbability < 0.6) return "HIGH";
        return "VERY_HIGH";
    }

    private double calculateMonthlyPayment(double amount, int months, double rate) {
        double monthlyRate = rate / 12;
        if (monthlyRate == 0) return amount / months;
        return amount * (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);
    }

    private String generateSuggestionReason(double riskScore, double requested, double suggested) {
        if (suggested > requested) {
            return "Low risk profile allows for higher credit limit";
        } else if (suggested < requested * 0.8) {
            return "High risk requires significant reduction for approval";
        } else if (suggested < requested) {
            return "Moderate risk suggests conservative amount";
        }
        return "Standard terms appropriate for risk profile";
    }

    private double calculateSimilarity(BorrowerProfile profile,
                                       CreditApplication application,
                                       BorrowerProfile otherProfile,
                                       AgriculturalCredit credit) {
        double incomeSim = 1 - Math.abs(profile.getMonthlyIncome() - otherProfile.getMonthlyIncome()) /
                Math.max(profile.getMonthlyIncome(), otherProfile.getMonthlyIncome());

        double debtSim = 1 - Math.abs(profile.getExistingDebt() - otherProfile.getExistingDebt()) /
                Math.max(profile.getExistingDebt(), otherProfile.getExistingDebt());

        double amountSim = 1 - Math.abs(application.getRequestedAmount() - credit.getApprovedAmount()) /
                Math.max(application.getRequestedAmount(), credit.getApprovedAmount());

        return (incomeSim + debtSim + amountSim) / 3.0;
    }

    // ===== DTOs =====

    @lombok.Data
    @lombok.Builder
    public static class MLPrediction {
        private boolean modelAvailable;
        private double defaultProbability;
        private double confidence;
        private String riskCategory;
    }

    @lombok.Data
    @lombok.Builder
    public static class LoanSuggestion {
        private double suggestedAmount;
        private int suggestedDuration;
        private double suggestedInterestRate;
        private double suggestedMonthlyPayment;
        private String reasoning;
    }

    @lombok.Data
    @lombok.Builder
    public static class SimilarFarmerCase {
        private double similarityScore;
        private double amount;
        private int duration;
        private String outcome;
    }
}