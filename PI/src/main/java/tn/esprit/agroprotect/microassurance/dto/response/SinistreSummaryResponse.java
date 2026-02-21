package tn.esprit.agroprotect.microassurance.dto.response;

import lombok.Data;
import tn.esprit.agroprotect.microassurance.enums.StatutSinistre;
import tn.esprit.agroprotect.microassurance.enums.TypeSinistre;

import java.time.Instant;

/**
 * DTO de résumé pour un sinistre
 */
@Data
public class SinistreSummaryResponse {

    private Long id;
    private TypeSinistre typeSinistre;
    private Instant dateDeclaration;
    private String description;
    private Long contratAssuranceId;
    private StatutSinistre statut;
    private Long createdByUserId;
}