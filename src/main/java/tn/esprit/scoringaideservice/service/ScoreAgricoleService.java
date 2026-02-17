package tn.esprit.scoringaideservice.service;
import java.util.List;

import tn.esprit.scoringaideservice.dto.EvolutionScoreDTO;
import tn.esprit.scoringaideservice.entity.RecommandationAgricole;
import tn.esprit.scoringaideservice.entity.ScoreAgricole;
import tn.esprit.scoringaideservice.dto.StatistiquesDTO;
import tn.esprit.scoringaideservice.dto.RecommandationDTO ;
import tn.esprit.scoringaideservice.dto.ScoreBreakdownDTO ;

public interface ScoreAgricoleService {

    /**
     * Calcule le score agricole pour un terrain donné
     * @param terrainId ID du terrain agricole
     * @return ScoreAgricole calculé avec recommandations
     */
    ScoreAgricole calculerScorePourTerrain(Long terrainId);

    List<ScoreAgricole> getHistoriqueScores(Long terrainId);
    ScoreBreakdownDTO getScoreBreakdown(Long terrainId);

    ScoreAgricole getDernierScore(Long terrainId);
    StatistiquesDTO getStatistiquesGlobales();
    List<EvolutionScoreDTO> getEvolutionScore(Long terrainId);
    List<RecommandationDTO> getRecommandationsDernierScore(Long terrainId);


}
