package tn.esprit.scoringaideservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.scoringaideservice.entity.*;
import tn.esprit.scoringaideservice.repository.*;
import tn.esprit.scoringaideservice.dto.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.scoringaideservice.dto.DecisionDTO;
import tn.esprit.scoringaideservice.dto.CropScoreComparisonDTO;
import org.springframework.data.domain.PageRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ScoreAgricoleServiceImpl implements ScoreAgricoleService {

    private final TerrainAgricoleRepository terrainAgricoleRepository;
    private final ScoreAgricoleRepository scoreAgricoleRepository;
    private final RecommandationAgricoleRepository recommandationAgricoleRepository;
    private final SatelliteService satelliteService;

    private final MarketLocalService marketLocalService;
    private final ScoringCalculator scoringCalculator;
    private final SoilService soilService;

    // =====================================================
    // ✅ 1️⃣ CALCULER SCORE INTELLIGENT (4 FACTEURS)
    // =====================================================
    @Override
    public ScoreAgricole calculerScorePourTerrain(Long terrainId) {

        TerrainAgricole terrain = terrainAgricoleRepository.findById(terrainId)
                .orElseThrow(() -> new RuntimeException("Terrain agricole introuvable"));

        // 🔹 Calcul des facteurs
        double agronomique = calculateAgronomique(terrain);
        double climatique = calculateClimatique(terrain);
        double productivite = calculateProductivite(terrain);
        double stabilite = calculateStabilite(terrain);

        // 🔹 Pondération globale
        double marketScore = marketLocalService.calculateMarketScore(terrain.getId());
        double scoreFinal = scoringCalculator.calculateFinalScore(
                agronomique,
                climatique,
                productivite,
                stabilite,
                marketScore
        );

        NiveauRisque niveau;

        if (scoreFinal >= 75) niveau = NiveauRisque.FAIBLE;
        else if (scoreFinal >= 50) niveau = NiveauRisque.MOYEN;
        else niveau = NiveauRisque.ELEVE;

        ScoreAgricole scoreAgricole = new ScoreAgricole();
        scoreAgricole.setScore(scoreFinal);
        scoreAgricole.setNiveau(niveau);
        scoreAgricole.setDateCalcul(LocalDate.now());
        scoreAgricole.setTerrainAgricole(terrain);

        scoreAgricole = scoreAgricoleRepository.save(scoreAgricole);

        List<RecommandationAgricole> recommandations =
                genererRecommandations(niveau, scoreAgricole);

        recommandationAgricoleRepository.saveAll(recommandations);
        scoreAgricole.setRecommandations(recommandations);

        return scoreAgricole;
    }

    // =====================================================
    // ✅ 2️⃣ HISTORIQUE
    // =====================================================
    @Override
    public List<ScoreAgricole> getHistoriqueScores(Long terrainId) {

        if (!terrainAgricoleRepository.existsById(terrainId))
            throw new RuntimeException("Terrain introuvable");

        return scoreAgricoleRepository
                .findByTerrainAgricoleIdOrderByDateCalculDesc(terrainId);
    }

    // =====================================================
    // ✅ 3️⃣ DERNIER SCORE
    // =====================================================
    @Override
    public ScoreAgricole getDernierScore(Long terrainId) {

        if (!terrainAgricoleRepository.existsById(terrainId))
            throw new RuntimeException("Terrain introuvable");

        return scoreAgricoleRepository
                .findTopByTerrainAgricoleIdOrderByDateCalculDesc(terrainId);
    }

    // =====================================================
    // ✅ 4️⃣ STATISTIQUES
    // =====================================================
    @Override
    public StatistiquesDTO getStatistiquesGlobales() {
        Map<String, Long> repartition = new HashMap<>();
        repartition.put("FAIBLE", scoreAgricoleRepository.countByNiveau(NiveauRisque.FAIBLE));
        repartition.put("MOYEN", scoreAgricoleRepository.countByNiveau(NiveauRisque.MOYEN));
        repartition.put("ELEVE", scoreAgricoleRepository.countByNiveau(NiveauRisque.ELEVE));

        Double avg = scoreAgricoleRepository.getAverageScore();
        
        return new StatistiquesDTO(
                terrainAgricoleRepository.count(),
                scoreAgricoleRepository.count(),
                avg != null ? avg : 0.0,
                scoreAgricoleRepository.countByNiveau(NiveauRisque.FAIBLE),
                scoreAgricoleRepository.countByNiveau(NiveauRisque.MOYEN),
                scoreAgricoleRepository.countByNiveau(NiveauRisque.ELEVE),
                repartition,
                new HashMap<>() // evolution could be added here if needed
        );
    }

    // =====================================================
    // ✅ 5️⃣ EVOLUTION
    // =====================================================
    @Override
    public List<EvolutionScoreDTO> getEvolutionScore(Long terrainId) {

        if (!terrainAgricoleRepository.existsById(terrainId))
            throw new RuntimeException("Terrain introuvable");

        List<ScoreAgricole> scores =
                scoreAgricoleRepository
                        .findByTerrainAgricoleIdOrderByDateCalculAsc(terrainId);

        List<EvolutionScoreDTO> evolution = new ArrayList<>();

        for (ScoreAgricole s : scores) {
            evolution.add(
                    new EvolutionScoreDTO(
                            s.getDateCalcul(),
                            s.getScore()
                    )
            );
        }

        return evolution;
    }

    // =====================================================
    // ✅ 6️⃣ RECOMMANDATIONS AVEC ÉTOILES
    // =====================================================
    @Override
    public List<RecommandationDTO> getRecommandationsDernierScore(Long terrainId) {

        ScoreAgricole dernierScore =
                scoreAgricoleRepository
                        .findTopByTerrainAgricoleIdOrderByDateCalculDesc(terrainId);

        if (dernierScore == null)
            throw new RuntimeException("Aucun score trouvé");

        List<RecommandationDTO> resultat = new ArrayList<>();

        String etoiles = convertirEnEtoiles(dernierScore.getNiveau());

        for (RecommandationAgricole rec : dernierScore.getRecommandations()) {
            resultat.add(
                    new RecommandationDTO(
                            rec.getDescription(),
                            dernierScore.getNiveau().name(),
                            etoiles
                    )
            );
        }

        return resultat;
    }

    @Override
    public ScoreBreakdownDTO getScoreBreakdown(Long terrainId) {

        TerrainAgricole terrain = terrainAgricoleRepository.findById(terrainId)
                .orElseThrow(() -> new RuntimeException("Terrain introuvable"));

        // 🔹 Calcul des facteurs
        double agronomique = calculateAgronomique(terrain);
        double climatique = calculateClimatique(terrain);
        double productivite = calculateProductivite(terrain);
        double stabilite = calculateStabilite(terrain);

        // 🔥 IMPORTANT : valeur neutre du marché
        double marketScore = marketLocalService.calculateMarketScore(terrain.getId());
        // 🔹 Score de base (inchangé)
        double finalScore = scoringCalculator.calculateFinalScore(
                agronomique,
                climatique,
                productivite,
                stabilite,
                marketScore
        );

        finalScore = round(finalScore);

        return new ScoreBreakdownDTO(
                agronomique,
                climatique,
                productivite,
                stabilite,
                marketScore,
                finalScore
        );
    }

    @Override
    public ScoreBreakdownDTO calculerScorePourCulture(
            Long terrainId,
            String cropName) {

        TerrainAgricole terrain = terrainAgricoleRepository
                .findById(terrainId)
                .orElseThrow(() ->
                        new RuntimeException("Terrain introuvable"));

        double agronomique = calculateAgronomique(terrain);
        double climatique = calculateClimatique(terrain);
        double productivite = calculateProductivite(terrain);
        double stabilite = calculateStabilite(terrain);

        double marketScore =
                marketLocalService
                        .calculateMarketScoreForCrop(cropName);

        // ✅ score initial (inchangé)
        double finalScore = scoringCalculator.calculateFinalScore(
                agronomique,
                climatique,
                productivite,
                stabilite,
                marketScore
        );

        return new ScoreBreakdownDTO(
                agronomique,
                climatique,
                productivite,
                stabilite,
                marketScore,
                finalScore
        );
    }
    @Override
    public CropScoreComparisonDTO comparerScoreAvecCulture(
            Long terrainId,
            String cropName) {

        ScoreBreakdownDTO scoreAvant =
                getScoreBreakdown(terrainId);

        ScoreBreakdownDTO scoreApres =
                calculerScorePourCulture(terrainId, cropName);

        double gain =
                scoreApres.getScoreFinal()
                        - scoreAvant.getScoreFinal();


        return new CropScoreComparisonDTO(
                round(scoreAvant.getScoreFinal()),
                round(scoreApres.getScoreFinal()),
                round(gain),
                scoreAvant,
                scoreApres
        );
    }

    @Override
    public DecisionDTO getDecision(Long terrainId) {

        ScoreAgricole score = calculerScorePourTerrain(terrainId);

        double valeur = score.getScore();
        String niveau = score.getNiveau().name();

        DecisionType decision;
        String justification;

        if (valeur >= 75) {
            decision = DecisionType.FINANCER;
            justification = "Projet rentable, faible risque.";
        } else if (valeur >= 50) {
            decision = DecisionType.SURVEILLER;
            justification = "Projet moyen, nécessite optimisation.";
        } else {
            decision = DecisionType.REFUSER;
            justification = "Risque élevé, conditions défavorables.";
        }

        return new DecisionDTO(
                valeur,
                niveau,
                decision.name(),
                justification
        );
    }

    @Override
    public List<ScoreAgricole> getTopTerrains(int limit) {
        List<ScoreAgricole> scores = scoreAgricoleRepository.findLatestScoresOrderByScoreDesc();
        
        // 🔥 Sécurité supplémentaire : Filtrer manuellement les doublons par ID de terrain
        return scores.stream()
                .filter(s -> s.getTerrainAgricole() != null)
                .collect(Collectors.toMap(
                        s -> s.getTerrainAgricole().getId(),
                        s -> s,
                        (s1, s2) -> s1, // Garder le premier (déjà trié par score)
                        java.util.LinkedHashMap::new
                ))
                .values().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 🔥 FACTEURS PROFESSIONNELS
    // =====================================================

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double calculateAgronomique(TerrainAgricole terrain) {

        try {

            SoilData soil = soilService.getSoilByTerrainId(terrain.getId());
            System.out.println("Soil API utilisée !");

            double score = 0;

            // 🔹 pH (idéal : 6 - 7.5)
            if (soil.getPh() >= 6 && soil.getPh() <= 7.5) score += 40;
            else if (soil.getPh() >= 5 && soil.getPh() <= 8) score += 25;
            else score += 10;

            // 🔹 Matière organique
            if (soil.getOrganicMatter() > 3) score += 30;
            else if (soil.getOrganicMatter() > 2) score += 20;
            else score += 10;

            // 🔹 Texture (équilibre sable/argile)
            if (soil.getClay() > 20 && soil.getSand() < 60) score += 30;
            else score += 15;

            return score;

        } catch (Exception e) {

            System.out.println("Fallback Soil !");
            // 🔥 fallback si API échoue
            return 50;
        }
    }


    private double calculateClimatique(TerrainAgricole terrain) {

        if (terrain.getDonneesClimatiques() == null ||
                terrain.getDonneesClimatiques().isEmpty())
            return 50; // valeur neutre si pas de données

        // 🔹 Trier par année décroissante
        List<DonneeClimatique> donneesTriees =
                terrain.getDonneesClimatiques()
                        .stream()
                        .sorted((d1, d2) ->
                                Integer.compare(
                                        d2.getAnnee() != null ? d2.getAnnee() : 0,
                                        d1.getAnnee() != null ? d1.getAnnee() : 0))
                        .toList();

        // 🔹 Prendre seulement les 3 dernières années
        int limite = Math.min(3, donneesTriees.size());

        double moyennePluie = donneesTriees.stream()
                .limit(limite)
                .mapToDouble(DonneeClimatique::getPluviometrie)
                .average()
                .orElse(0);

        double score;

        if (moyennePluie >= 40) score = 90;
        else if (moyennePluie >= 25) score = 70;
        else if (moyennePluie >= 15) score = 50;
        else score = 30;

        return score; // score sur 100
    }




    private double calculateProductivite(TerrainAgricole terrain) {

        double scoreCulture = 50;

        // 🔹 Rendement base (BDD)
        if (terrain.getCultures() != null && !terrain.getCultures().isEmpty()) {

            double rendement = terrain.getCultures()
                    .stream()
                    .mapToDouble(Culture::getRendementEstime)
                    .average()
                    .orElse(0);

            if (rendement >= 80) scoreCulture = 90;
            else if (rendement >= 60) scoreCulture = 75;
            else if (rendement >= 40) scoreCulture = 60;
            else scoreCulture = 40;
        }

        // 🔥 Injecter rendement satellite
        try {

            SatelliteBiomasseDTO biomasse =
                    satelliteService.getBiomasse(terrain.getId());

            double rendementSatellite = biomasse.getRendementEstime();

            double scoreSatellite;

            if (rendementSatellite >= 8) scoreSatellite = 95;
            else if (rendementSatellite >= 6) scoreSatellite = 80;
            else if (rendementSatellite >= 4) scoreSatellite = 60;
            else scoreSatellite = 40;

            // 🔥 Fusion intelligente
            return (scoreCulture * 0.6) + (scoreSatellite * 0.4);

        } catch (Exception e) {

            // fallback si satellite échoue
            return scoreCulture;
        }
    }



    private double calculateStabilite(TerrainAgricole terrain) {

        double scoreHistorique = 70;

        List<ScoreAgricole> historique =
                scoreAgricoleRepository
                        .findByTerrainAgricoleIdOrderByDateCalculDesc(terrain.getId());

        if (historique.size() >= 2) {

            double variation =
                    Math.abs(historique.get(0).getScore()
                            - historique.get(1).getScore());

            if (variation <= 5) scoreHistorique = 95;
            else if (variation <= 15) scoreHistorique = 75;
            else if (variation <= 25) scoreHistorique = 55;
            else scoreHistorique = 35;
        }

        // 🔥 Injecter stabilité NDVI
        try {

            List<SatelliteEvolutionDTO> evolution =
                    satelliteService.getEvolution(terrain.getId());

            if (evolution.size() >= 2) {

                double variationNdvi =
                        Math.abs(evolution.get(0).getNdvi()
                                - evolution.get(1).getNdvi());

                double scoreSatellite;

                if (variationNdvi <= 0.05) scoreSatellite = 95;
                else if (variationNdvi <= 0.15) scoreSatellite = 75;
                else if (variationNdvi <= 0.25) scoreSatellite = 55;
                else scoreSatellite = 35;

                return (scoreHistorique * 0.6) + (scoreSatellite * 0.4);
            }

        } catch (Exception e) {
            return scoreHistorique;
        }

        return scoreHistorique;
    }



    // =====================================================
    // 🔥 UTILITAIRE
    // =====================================================
    private String convertirEnEtoiles(NiveauRisque niveau) {

        switch (niveau) {
            case FAIBLE: return "⭐⭐⭐⭐⭐";
            case MOYEN: return "⭐⭐⭐";
            case ELEVE: return "⭐";
            default: return "";
        }
    }

    private List<RecommandationAgricole> genererRecommandations(
            NiveauRisque niveau,
            ScoreAgricole scoreAgricole) {

        List<RecommandationAgricole> recommandations = new ArrayList<>();

        switch (niveau) {
            case ELEVE:
                recommandations.add(creerRecommandation(
                        "Risque élevé : améliorer le sol et réduire les charges.",
                        scoreAgricole));
                break;

            case MOYEN:
                recommandations.add(creerRecommandation(
                        "Risque moyen : surveiller le climat et optimiser l’irrigation.",
                        scoreAgricole));
                break;

            case FAIBLE:
                recommandations.add(creerRecommandation(
                        "Projet stable et favorable à l’investissement.",
                        scoreAgricole));
                break;
        }

        return recommandations;
    }

    private RecommandationAgricole creerRecommandation(
            String description,
            ScoreAgricole scoreAgricole) {

        RecommandationAgricole rec = new RecommandationAgricole();
        rec.setDescription(description);
        rec.setScoreAgricole(scoreAgricole);
        return rec;
    }
}
