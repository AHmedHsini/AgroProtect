package tn.esprit.scoringaideservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ScoreAgricole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;

    @Enumerated(EnumType.STRING)
    private NiveauRisque niveau;

    private LocalDate dateCalcul;

    /* =======================
       Relations
       ======================= */

    @ManyToOne
    @JoinColumn(name = "terrain_id")
    private TerrainAgricole terrainAgricole;


    // 🔥 IMPORTANT POUR ÉVITER BOUCLE INFINIE
    @OneToMany(mappedBy = "scoreAgricole", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<RecommandationAgricole> recommandations;
}
