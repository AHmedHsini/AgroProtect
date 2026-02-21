package tn.esprit.agroprotect.microassurance.enums;

/**
 * Statut d'une indemnisation
 */
public enum StatutIndemnisation {
    EN_ATTENTE("En attente"),
    PAYE("Payé"),
    ANNULE("Annulé");

    private final String description;

    StatutIndemnisation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}