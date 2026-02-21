package tn.esprit.agroprotect.microassurance.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import tn.esprit.agroprotect.microassurance.enums.TypeSinistre;

import java.math.BigDecimal;

/**
 * DTO pour la création d'un sinistre
 */
@Data
public class CreateSinistreRequest {

    @NotNull(message = "Le type de sinistre est obligatoire")
    private TypeSinistre typeSinistre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Positive(message = "L'ID du contrat d'assurance doit être positif")
    private Long contratAssuranceId;

    @DecimalMin(value = "0.0", inclusive = false, message = "L'estimation de perte doit être positive")
    @Digits(integer = 13, fraction = 2, message = "L'estimation de perte doit avoir au maximum 13 chiffres avant la virgule et 2 après")
    private BigDecimal estimationPerte;
}