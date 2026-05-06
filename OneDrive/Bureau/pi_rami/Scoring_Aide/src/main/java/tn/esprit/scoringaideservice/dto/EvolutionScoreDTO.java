package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EvolutionScoreDTO {

    private LocalDate date;
    private Double score;

}
