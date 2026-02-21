package tn.esprit.agroprotect.microassurance.enums;

/**
 * Statut d'un sinistre
 */
public enum StatutSinistre {
    DECLARE("Déclaré"),
    EN_EVALUATION("En évaluation"),
    VALIDE("Validé"),
    REFUSE("Refusé");

    private final String description;

    StatutSinistre(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}