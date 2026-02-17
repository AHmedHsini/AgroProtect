package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScoreBreakdownDTO {

    private double agronomique;
    private double climatique;
    private double productivite;
    private double stabilite;
    private double scoreFinal;
}
