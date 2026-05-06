package tn.esprit.scoringaideservice.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Agriculteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String prenom;

    private String region;

    private String telephone;

    /* =======================
       Relations
       ======================= */

    // Un agriculteur possède plusieurs terrains
    @OneToMany(mappedBy = "agriculteur", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TerrainAgricole> terrains;
}
