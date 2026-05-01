package  tn.esprit.agroprotect.Marketplace.dtos;

import  tn.esprit.agroprotect.Marketplace.entities.Annonce;
import  tn.esprit.agroprotect.Marketplace.entities.StatusAnnonce;
import  tn.esprit.agroprotect.Marketplace.entities.TypeAnnonce;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnnonceSearchResponse {

    private List<AnnonceItem> content;
    private PaginationInfo pagination;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnnonceItem {
        private Long id;
        private TypeAnnonce typeAnnonce;
        private String titre;
        private String description;
        private StatusAnnonce status;
        private LocalDateTime datePublication;
        private Long createurId;
        private Double targetAmount;
        private Integer lastMilestoneNotified;
        private String location;
        private Integer targetDurationMonths;
        private LocalDateTime lastModified;
        private Double fundedAmount;
        private Double progressPercentage;
        private Integer totalInvestors;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaginationInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
