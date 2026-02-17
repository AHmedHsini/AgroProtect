package tn.esprit.scoringaideservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeteoHistoriqueDTO {

    private Integer annee;
    private Double temperatureMoyenne;
    private Double pluieTotale;

}
