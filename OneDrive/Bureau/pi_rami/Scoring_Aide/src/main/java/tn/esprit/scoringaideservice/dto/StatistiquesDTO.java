package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatistiquesDTO {

    private long totalTerrains;
    private long totalScores;
    private double scoreMoyenGlobal;
    private long risqueFaible;
    private long risqueMoyen;
    private long risqueEleve;
    private Map<String, Long> repartitionRisque;
    private Map<String, Double> evolutionMoyenne;
}
