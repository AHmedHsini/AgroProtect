package  tn.esprit.agroprotect.Marketplace.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FundingProgressDTO {

    private Long annonceId;
    private String titre;
    private Double targetAmount;
    private Double fundedAmount;
    private Double progressPercentage;
    private Integer totalInvestors;
    private Integer acceptedInvestors;
    private Integer pendingInvestors;
    private String status;
}