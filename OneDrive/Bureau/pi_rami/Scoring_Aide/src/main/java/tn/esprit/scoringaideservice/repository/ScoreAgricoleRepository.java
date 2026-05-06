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

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s FROM ScoreAgricole s WHERE s.id IN (SELECT MAX(s2.id) FROM ScoreAgricole s2 GROUP BY s2.terrainAgricole.id) ORDER BY s.score DESC")
    List<ScoreAgricole> findLatestScoresOrderByScoreDesc();

    @org.springframework.data.jpa.repository.Query("SELECT AVG(s.score) FROM ScoreAgricole s")
    Double getAverageScore();
}
