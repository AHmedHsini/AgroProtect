package tn.esprit.agroprotect.microassurance.dto.response;

import lombok.Data;
import tn.esprit.agroprotect.microassurance.enums.StatutIndemnisation;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de résumé pour une indemnisation
 */
@Data
public class IndemnisationSummaryResponse {

    private Long id;
    private BigDecimal montant;
    private Instant dateCreation;
    private Instant datePaiement;
    private StatutIndemnisation statut;
    private String paymentReference;
}