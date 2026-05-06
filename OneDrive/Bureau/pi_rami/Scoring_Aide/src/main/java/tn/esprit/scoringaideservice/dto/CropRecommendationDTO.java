package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CropRecommendationDTO {

    private String cropName;

    private double localNeedScore;

    private double soilCompatibilityScore;

    private double climateCompatibilityScore;

    private double finalOpportunityScore;

    private String justification;
}