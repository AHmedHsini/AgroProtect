package tn.esprit.scoringaideservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.scoringaideservice.entity.ScoreAgricole;
import tn.esprit.scoringaideservice.entity.NiveauRisque;

import java.util.List;

public interface ScoreAgricoleRepository
        extends JpaRepository<ScoreAgricole, Long> {

    List<ScoreAgricole>
    findByTerrainAgricoleIdOrderByDateCalculDesc(Long terrainId);

    ScoreAgricole
    findTopByTerrainAgricoleIdOrderByDateCalculDesc(Long terrainId);
    long countByNiveau(NiveauRisque niveau);

    List<ScoreAgricole>
    findByTerrainAgricoleIdOrderByDateCalculAsc(Long terrainId);


}
