package tn.esprit.agroprotect.Marketplace.dtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import  tn.esprit.agroprotect.Marketplace.entities.StatusAnnonce;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateAnnonceRequest {
    private String titre;
    private String description;
    private StatusAnnonce status;
    private Double targetAmount;  // ← Explicitly included
    private String location;
    private Integer targetDurationMonths;
    // getters/setters...
}