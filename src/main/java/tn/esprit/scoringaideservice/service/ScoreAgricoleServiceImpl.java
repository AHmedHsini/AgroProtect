
        package tn.esprit.scoringaideservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.scoringaideservice.entity.*;
import tn.esprit.scoringaideservice.repository.*;
import tn.esprit.scoringaideservice.dto.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreAgricoleServiceImpl implements ScoreAgricoleService {

    private final TerrainAgricoleRepository terrainAgricoleRepository;
    private final ScoreAgricoleRepository scoreAgricoleRepository;
    private final RecommandationAgricoleRepository recommandationAgricoleRepository;
    private final SatelliteService satelliteService;

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
        double scoreFinal =
                (agronomique * 0.25) +
                        (climatique * 0.25) +
                        (productivite * 0.30) +
                        (stabilite * 0.20);

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

        return new StatistiquesDTO(
                terrainAgricoleRepository.count(),
                scoreAgricoleRepository.count(),
                scoreAgricoleRepository.countByNiveau(NiveauRisque.FAIBLE),
                scoreAgricoleRepository.countByNiveau(NiveauRisque.MOYEN),
                scoreAgricoleRepository.countByNiveau(NiveauRisque.ELEVE)
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

        double agronomique = calculateAgronomique(terrain);
        double climatique = calculateClimatique(terrain);
        double productivite = calculateProductivite(terrain);
        double stabilite = calculateStabilite(terrain);

        double scoreFinal =
                (agronomique * 0.25) +
                        (climatique * 0.25) +
                        (productivite * 0.30) +
                        (stabilite * 0.20);

        return new ScoreBreakdownDTO(
                agronomique,
                climatique,
                productivite,
                stabilite,
                scoreFinal
        );
    }

    // =====================================================
    // 🔥 FACTEURS PROFESSIONNELS
    // =====================================================

    private double calculateAgronomique(TerrainAgricole terrain) {

        double score = 0;

        if (terrain.getSurface() >= 20) score += 50;
        else if (terrain.getSurface() >= 10) score += 35;
        else score += 20;

        if ("fertile".equalsIgnoreCase(terrain.getTypeSol())) score += 50;
        else if ("argileux".equalsIgnoreCase(terrain.getTypeSol())) score += 30;
        else score += 15;

        return score; // max 100
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
