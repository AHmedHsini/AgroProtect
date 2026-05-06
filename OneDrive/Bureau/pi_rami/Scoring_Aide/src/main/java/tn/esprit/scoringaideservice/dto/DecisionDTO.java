package tn.esprit.scoringaideservice.dto;

public class DecisionDTO {
    private double score;
    private String niveau;
    private String decision;
    private String justification;

    public DecisionDTO(double score, String niveau, String decision, String justification) {
        this.score = score;
        this.niveau = niveau;
        this.decision = decision;
        this.justification = justification;
    }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}