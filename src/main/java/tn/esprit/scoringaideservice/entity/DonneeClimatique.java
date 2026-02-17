package tn.esprit.scoringaideservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DonneeClimatique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer annee;
    private Double temperatureMoyenne;
    private Double pluviometrie;
    private Double humiditeMoyenne;

    @ManyToOne
    @JoinColumn(name = "terrain_agricole_id")
    private TerrainAgricole terrainAgricole;
}

