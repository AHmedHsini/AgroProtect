package tn.esprit.spring1.dto;

import lombok.Data;
import java.util.Map;

@Data
public class MLResponse {

    private double probability;
    private int success;
    private Map<String, Double> explanation;
}