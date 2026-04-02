package tn.esprit.spring1.dto;

import lombok.Data;
import tn.esprit.spring1.entities.Investment;

import java.util.List;

@Data
public class AIResult {

    private double success;
    private String explanation;
    private List<Investment> investments;
}