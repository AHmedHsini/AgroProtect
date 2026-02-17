package tn.esprit.scoringaideservice.dto;

import lombok.Data;

@Data
public class MeteoDTO {

    private String ville;
    private Double temperature;
    private Double temperatureRessentie;
    private Integer humidity;
    private Double windSpeed;
    private Integer cloudiness;
    private String description;
    private String resume;
}
