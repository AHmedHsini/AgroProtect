package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CropScoreComparisonDTO {
    private double scoreAvant;
    private double scoreApres;
    private double gain;
    private ScoreBreakdownDTO detailsAvant;
    private ScoreBreakdownDTO detailsApres;
}