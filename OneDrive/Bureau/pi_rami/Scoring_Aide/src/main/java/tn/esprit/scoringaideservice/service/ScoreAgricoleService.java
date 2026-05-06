package tn.esprit.scoringaideservice.service;

import java.util.List;
import tn.esprit.scoringaideservice.dto.*;
import tn.esprit.scoringaideservice.entity.*;

public interface ScoreAgricoleService {
    ScoreAgricole calculerScorePourTerrain(Long terrainId);
    List<ScoreAgricole> getHistoriqueScores(Long terrainId);
    ScoreBreakdownDTO getScoreBreakdown(Long terrainId);
    ScoreAgricole getDernierScore(Long terrainId);
    StatistiquesDTO getStatistiquesGlobales();
    List<EvolutionScoreDTO> getEvolutionScore(Long terrainId);
    List<RecommandationDTO> getRecommandationsDernierScore(Long terrainId);
    ScoreBreakdownDTO calculerScorePourCulture(Long terrainId, String cropName);
    CropScoreComparisonDTO comparerScoreAvecCulture(Long terrainId, String cropName);
    DecisionDTO getDecision(Long terrainId);
    List<ScoreAgricole> getTopTerrains(int limit);
}