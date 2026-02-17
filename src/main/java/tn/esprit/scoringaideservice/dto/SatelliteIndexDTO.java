package tn.esprit.scoringaideservice.dto;

import lombok.Data;

@Data
public class SatelliteIndexDTO {

    private double ndvi;
    private double evi;
    private double ndwi;

    private String niveauSante;
}
