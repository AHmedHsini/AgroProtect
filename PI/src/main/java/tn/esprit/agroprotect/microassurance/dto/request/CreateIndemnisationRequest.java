package tn.esprit.agroprotect.microassurance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO pour créer une indemnisation
 */
@Data
public class CreateIndemnisationRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    @Digits(integer = 13, fraction = 2, message = "Le montant doit avoir au maximum 13 chiffres avant la virgule et 2 après")
    private BigDecimal montant;
}