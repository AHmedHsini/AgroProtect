package tn.esprit.agroprotect.microassurance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour refuser un sinistre
 */
@Data
public class RefuseSinistreRequest {

    @NotBlank(message = "Le motif de refus est obligatoire")
    @Size(max = 500, message = "Le motif de refus ne peut pas dépasser 500 caractères")
    private String motifRefus;
}