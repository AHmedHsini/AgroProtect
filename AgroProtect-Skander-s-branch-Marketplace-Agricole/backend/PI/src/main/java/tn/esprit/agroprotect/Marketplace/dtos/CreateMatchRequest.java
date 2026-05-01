package tn.esprit.agroprotect.Marketplace.dtos;

import tn.esprit.agroprotect.Marketplace.entities.StatusMatch;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateMatchRequest {

    @NotNull
    private Long annonceId;  // ← Just the ID

    @NotNull
    private Long investisseurId;

    private String message;

    private Double montantPropose;

    private StatusMatch status;  // Optional, defaults to EN_ATTENTE
}