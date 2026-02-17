package tn.esprit.scoringaideservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SatelliteEvolutionDTO {

    private LocalDate date;
    private double ndvi;
}
