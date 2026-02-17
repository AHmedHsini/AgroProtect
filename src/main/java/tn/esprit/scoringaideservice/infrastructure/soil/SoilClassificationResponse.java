package tn.esprit.scoringaideservice.infrastructure.soil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SoilClassificationResponse {

    @JsonProperty("wrb_class_name")
    private String wrbClassName;

    @JsonProperty("probability")
    private double probability;

    public String getWrbClassName() {
        return wrbClassName;
    }

    public void setWrbClassName(String wrbClassName) {
        this.wrbClassName = wrbClassName;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
