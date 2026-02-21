package tn.esprit.agroprotect.microassurance.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO pour valider un sinistre
 */
@Data
public class ValidateSinistreRequest {

    @DecimalMin(value = "0.0", message = "Le taux de remboursement doit être positif")
    @DecimalMax(value = "1.0", message = "Le taux de remboursement ne peut pas dépasser 100%")
    @Digits(integer = 1, fraction = 2, message = "Le taux de remboursement doit être un pourcentage avec au maximum 2 décimales")
    private BigDecimal tauxRemboursement;
}