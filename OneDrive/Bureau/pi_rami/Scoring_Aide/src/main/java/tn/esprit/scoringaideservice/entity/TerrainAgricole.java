package tn.esprit.scoringaideservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TerrainAgricole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double surface;
    private String typeSol;
    private String region;

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String geometryJson;

    private Long eosFieldId;

    /* =======================
       Relations
       ======================= */

    @ManyToOne
    @JoinColumn(name = "agriculteur_id")
    private Agriculteur agriculteur;

    @OneToMany(mappedBy = "terrainAgricole", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Culture> cultures;

    @OneToMany(mappedBy = "terrainAgricole", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DonneeClimatique> donneesClimatiques;

    // 🔥 IMPORTANT POUR ÉVITER BOUCLE INFINIE
    @OneToMany(mappedBy = "terrainAgricole", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ScoreAgricole> scoresAgricoles;
}
