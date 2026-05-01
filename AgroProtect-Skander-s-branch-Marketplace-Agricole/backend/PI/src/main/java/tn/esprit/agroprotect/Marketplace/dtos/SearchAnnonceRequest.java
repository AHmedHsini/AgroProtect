package  tn.esprit.agroprotect.Marketplace.dtos;

import  tn.esprit.agroprotect.Marketplace.entities.StatusAnnonce;
import  tn.esprit.agroprotect.Marketplace.entities.TypeAnnonce;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchAnnonceRequest {

    private String search;
    private TypeAnnonce type;
    private StatusAnnonce status;
    private String location;
    private Double minAmount;
    private Double maxAmount;
    private int page = 0;
    private int size = 10;
    private String sortBy = "datePublication";
    private boolean sortDesc = true;
}
