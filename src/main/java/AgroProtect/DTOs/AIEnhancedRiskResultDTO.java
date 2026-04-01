package AgroProtect.DTOs;

import AgroProtect.services.CreditRiskMLService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "AI-enhanced credit risk evaluation result")
public class AIEnhancedRiskResultDTO {
    private Long applicationId;
    private double ruleBasedScore;
    private double mlDefaultProbability;
    private double mlConfidence;
    private boolean mlAvailable;
    private double combinedScore;
    private String finalDecision;
    private double aiSuggestedAmount;
    private int aiSuggestedDuration;
    private double aiSuggestedRate;
    private String aiReasoning;
    private List<CreditRiskMLService.SimilarFarmerCase> similarCases;
    private List<String> riskFactors;
}