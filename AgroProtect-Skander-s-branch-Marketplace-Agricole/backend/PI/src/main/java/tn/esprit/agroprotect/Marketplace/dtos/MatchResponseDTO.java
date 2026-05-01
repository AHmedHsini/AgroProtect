package  tn.esprit.agroprotect.Marketplace.dtos;

import  tn.esprit.agroprotect.Marketplace.entities.StatusMatch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MatchResponseDTO {

    private Long id;
    private Long annonceId;
    private String annonceTitre;
    private Long investisseurId;
    private LocalDateTime matchDate;
    private StatusMatch status;
    private String message;
    private Double montantPropose;
}