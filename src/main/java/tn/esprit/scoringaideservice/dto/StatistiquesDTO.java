package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatistiquesDTO {

    private long totalTerrains;
    private long totalScores;
    private long risqueFaible;
    private long risqueMoyen;
    private long risqueEleve;
}
