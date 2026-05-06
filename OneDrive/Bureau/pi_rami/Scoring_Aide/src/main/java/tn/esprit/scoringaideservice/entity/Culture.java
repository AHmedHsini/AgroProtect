package tn.esprit.scoringaideservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Culture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String saison;

    private Double rendementEstime;

    /* =======================
       Relations
       ======================= */

    @ManyToOne
    private TerrainAgricole terrainAgricole;
}
