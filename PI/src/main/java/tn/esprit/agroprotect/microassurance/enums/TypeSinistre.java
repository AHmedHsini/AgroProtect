package tn.esprit.agroprotect.microassurance.enums;

/**
 * Types de sinistres dans le domaine agricole
 */
public enum TypeSinistre {
    CLIMAT("Sinistre climatique"),
    SECHERESSE("Sécheresse"),
    INONDATION("Inondation"),
    INCENDIE("Incendie"),
    RENDEMENT("Perte de rendement"),
    MALADIE("Maladie des plantes"),
    PARASITES("Parasites et ravageurs");

    private final String description;

    TypeSinistre(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}