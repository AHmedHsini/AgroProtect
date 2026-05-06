package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalNeedDTO {

    private String crop;

    private double importVolume;

    private double productionTrend;

    private double strategicWeight;

    private double localNeedScore;
}