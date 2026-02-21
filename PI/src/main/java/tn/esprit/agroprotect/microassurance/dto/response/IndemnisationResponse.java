package tn.esprit.agroprotect.microassurance.dto.response;

import lombok.Data;
import tn.esprit.agroprotect.microassurance.enums.StatutIndemnisation;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de réponse pour une indemnisation
 */
@Data
public class IndemnisationResponse {

    private Long id;
    private Long sinistreId;
    private BigDecimal montant;
    private Instant dateCreation;
    private Instant datePaiement;
    private StatutIndemnisation statut;
    private String paymentReference;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    
    // Information résumée du sinistre associé
    private SinistreSummaryResponse sinistre;
}