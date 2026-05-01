package tn.esprit.agroprotect.Marketplace.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.agroprotect.Marketplace.entities.StatusMatch;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateMatchRequest {
    @NotNull
    private Long annonceId;  // ← Just the ID



    private String message;

    private Double montantPropose;

    private StatusMatch status;  // Optional, defaults to EN_ATTENTE
}
