package tn.esprit.agroprotect.microassurance.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO pour effectuer un paiement d'indemnisation
 */
@Data
public class PayIndemnisationRequest {

    @Size(max = 100, message = "La référence de paiement ne peut pas dépasser 100 caractères")
    private String paymentReference;
}