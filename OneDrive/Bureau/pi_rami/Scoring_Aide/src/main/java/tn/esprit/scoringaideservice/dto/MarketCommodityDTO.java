package tn.esprit.scoringaideservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketCommodityDTO {

    private String name;

    private double production;

    private double imports;

    private double exports;

    private double importDependencyRatio;
}