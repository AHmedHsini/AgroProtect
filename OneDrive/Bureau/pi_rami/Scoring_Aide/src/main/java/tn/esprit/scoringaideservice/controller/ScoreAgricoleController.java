package tn.esprit.scoringaideservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.scoringaideservice.entity.ScoreAgricole;
import tn.esprit.scoringaideservice.service.ScoreAgricoleService;
import tn.esprit.scoringaideservice.dto.StatistiquesDTO;
import tn.esprit.scoringaideservice.dto.EvolutionScoreDTO;
import tn.esprit.scoringaideservice.dto.RecommandationDTO;
import tn.esprit.scoringaideservice.dto.ScoreBreakdownDTO;
import tn.esprit.scoringaideservice.dto.CropScoreComparisonDTO;
import tn.esprit.scoringaideservice.dto.DecisionDTO;
import tn.esprit.scoringaideservice.entity.TerrainAgricole;
import java.util.List;

@RestController
@RequestMapping("/api/scoring")
@RequiredArgsConstructor
public class ScoreAgricoleController {

    private final ScoreAgricoleService scoreAgricoleService;

    @PostMapping("/calculer/{terrainId}")
    public ScoreAgricole calculerScore(@PathVariable Long terrainId) {
        return scoreAgricoleService.calculerScorePourTerrain(terrainId);
    }

    @GetMapping("/historique/{terrainId}")
    public List<ScoreAgricole> getHistorique(@PathVariable Long terrainId) {
        return scoreAgricoleService.getHistoriqueScores(terrainId);
    }

    @GetMapping("/dernier/{terrainId}")
    public ScoreAgricole getDernier(@PathVariable Long terrainId) {
        return scoreAgricoleService.getDernierScore(terrainId);
    }

    @GetMapping("/statistiques")
    public StatistiquesDTO getStatistiques() {
        return scoreAgricoleService.getStatistiquesGlobales();
    }

    @GetMapping("/evolution/{terrainId}")
    public List<EvolutionScoreDTO> getEvolution(@PathVariable Long terrainId) {
        return scoreAgricoleService.getEvolutionScore(terrainId);
    }

    @GetMapping("/recommandations/{terrainId}")
    public List<RecommandationDTO> getRecommandations(@PathVariable Long terrainId) {
        return scoreAgricoleService.getRecommandationsDernierScore(terrainId);
    }

    @GetMapping("/breakdown/{terrainId}")
    public ScoreBreakdownDTO getBreakdown(@PathVariable Long terrainId) {
        return scoreAgricoleService.getScoreBreakdown(terrainId);
    }

    @GetMapping("/crop/{terrainId}")
    public CropScoreComparisonDTO comparerScore(
            @PathVariable Long terrainId,
            @RequestParam String crop) {
        return scoreAgricoleService.comparerScoreAvecCulture(terrainId, crop);
    }

    @GetMapping("/decision/{terrainId}")
    public DecisionDTO getDecision(@PathVariable Long terrainId) {
        return scoreAgricoleService.getDecision(terrainId);
    }

    @GetMapping("/top-terrains")
    public List<ScoreAgricole> getTopTerrains(@RequestParam(defaultValue = "5") int limit) {
        return scoreAgricoleService.getTopTerrains(limit);
    }
}