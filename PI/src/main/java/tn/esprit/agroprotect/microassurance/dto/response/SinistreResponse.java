package tn.esprit.agroprotect.microassurance.dto.response;

import lombok.Data;
import tn.esprit.agroprotect.microassurance.enums.StatutSinistre;
import tn.esprit.agroprotect.microassurance.enums.TypeSinistre;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de réponse pour un sinistre
 */
@Data
public class SinistreResponse {

    private Long id;
    private TypeSinistre typeSinistre;
    private Instant dateDeclaration;
    private String description;
    private Long contratAssuranceId;
    private StatutSinistre statut;
    private String motifRefus;
    private BigDecimal tauxRemboursement;
    private BigDecimal estimationPerte;
    private Long createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    
    // Information sur l'indemnisation associée si elle existe
    private IndemnisationSummaryResponse indemnisation;
}