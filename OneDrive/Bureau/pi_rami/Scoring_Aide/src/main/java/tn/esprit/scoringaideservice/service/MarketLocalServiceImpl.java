package tn.esprit.scoringaideservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.scoringaideservice.dto.MarketCommodityDTO;
import tn.esprit.scoringaideservice.dto.CropRecommendationDTO;
import tn.esprit.scoringaideservice.entity.TerrainAgricole;
import tn.esprit.scoringaideservice.external.FaoClient;
import tn.esprit.scoringaideservice.repository.TerrainAgricoleRepository;
import tn.esprit.scoringaideservice.dto.SoilData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketLocalServiceImpl implements MarketLocalService {

    private final FaoClient faoClient;
    private final TerrainAgricoleRepository terrainRepository;
    private final SoilService soilService;

    // =====================================================
    // ✅ 1️⃣ DEPENDANCE IMPORTATION
    // =====================================================
    @Override
    public List<MarketCommodityDTO> getMarketDependency() {
        try {
            return faoClient.fetchTunisiaMarketData();
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des données FAO", e);
            return new ArrayList<>(); // Return empty list instead of crashing
        }
    }

    // =====================================================
    // ✅ 2️⃣ OPPORTUNITÉS POUR UN TERRAIN
    // =====================================================
    @Override
    public List<CropRecommendationDTO> getOpportunities(Long terrainId) {
        try {
            TerrainAgricole terrain = terrainRepository.findById(terrainId).orElse(null);

            if (terrain == null) {
                log.warn("Terrain introuvable avec l'ID: {}. Retourne des opportunités par défaut.", terrainId);
                return buildDefaultOpportunities();
            }

            List<MarketCommodityDTO> commodities = faoClient.fetchTunisiaMarketData();
            if (commodities == null || commodities.isEmpty()) {
                log.warn("Aucune donnée marché disponible. Retourne fallback.");
                return buildDefaultOpportunities();
            }

            return commodities.stream()
                    .map(crop -> buildRecommendation(crop, terrain))
                    .sorted(Comparator.comparingDouble(CropRecommendationDTO::getFinalOpportunityScore).reversed())
                    .limit(3)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erreur critique lors du calcul des opportunités pour le terrain {}", terrainId, e);
            return buildDefaultOpportunities();
        }
    }

    private List<CropRecommendationDTO> buildDefaultOpportunities() {
        List<CropRecommendationDTO> list = new ArrayList<>();
        list.add(new CropRecommendationDTO("Blé tendre (Simulation)", 80, 50, 50, 60, "Forte dépendance simulée"));
        list.add(new CropRecommendationDTO("Orge (Simulation)", 70, 60, 60, 65, "Simulation de marché"));
        return list;
    }

    @Override
    public double calculateMarketScoreForCrop(String cropName) {
        try {
            List<MarketCommodityDTO> commodities = faoClient.fetchTunisiaMarketData();

            MarketCommodityDTO commodity = commodities.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(cropName))
                    .findFirst()
                    .orElse(null);

            if (commodity == null) {
                log.warn("Culture non trouvée: {}", cropName);
                return 50.0; // Score par défaut
            }

            double importDependency = commodity.getImportDependencyRatio();
            double normalizedImportVolume = commodity.getImports() / 1_500_000.0;
            double productionTrend = 0.5; // simulé

            double localNeedScore = (importDependency * 0.5) + (normalizedImportVolume * 0.3) + (productionTrend * 0.2);

            return localNeedScore * 100;
        } catch (Exception e) {
            log.error("Erreur calcul market score crop {}", cropName, e);
            return 50.0;
        }
    }

    @Override
    public double calculateMarketScore(Long terrainId) {
        try {
            List<MarketCommodityDTO> commodities = faoClient.fetchTunisiaMarketData();

            return commodities.stream()
                    .mapToDouble(c -> {
                        double importDependency = c.getImportDependencyRatio();
                        double normalizedImport = c.getImports() / 1_500_000.0;
                        double trend = 0.5;
                        double score = (importDependency * 0.5) + (normalizedImport * 0.3) + (trend * 0.2);
                        return score * 100;
                    })
                    .average()
                    .orElse(50);
        } catch (Exception e) {
            log.error("Erreur calcul market score terrain {}", terrainId, e);
            return 50.0;
        }
    }

    // =====================================================
    // 🔥 LOGIQUE MÉTIER PRINCIPALE
    // =====================================================
    private CropRecommendationDTO buildRecommendation(MarketCommodityDTO commodity, TerrainAgricole terrain) {
        double importDependency = commodity.getImportDependencyRatio();
        double normalizedImportVolume = commodity.getImports() / 1_500_000.0; 
        double productionTrend = 0.5; 

        double localNeedScore = (importDependency * 0.5) + (normalizedImportVolume * 0.3) + (productionTrend * 0.2);

        double soilScore = calculateSoilCompatibility(terrain);
        double climateScore = calculateClimateCompatibility(terrain);

        double finalScore = (localNeedScore * 0.6) + (soilScore * 0.25) + (climateScore * 0.15);

        return new CropRecommendationDTO(
                commodity.getName(),
                round(localNeedScore * 100),
                round(soilScore * 100),
                round(climateScore * 100),
                round(finalScore * 100),
                "Forte dépendance import + compatibilité terrain"
        );
    }

    // =====================================================
    // 🌱 COMPATIBILITÉ SOL
    // =====================================================
    private double calculateSoilCompatibility(TerrainAgricole terrain) {
        try {
            SoilData soil = soilService.getSoilByTerrainId(terrain.getId());
            if (soil == null) return 0.5;

            double score = 0;

            if (soil.getPh() >= 6 && soil.getPh() <= 7.5) score += 0.4;
            else score += 0.2;

            if (soil.getOrganicMatter() > 3) score += 0.3;
            else score += 0.2;

            if (soil.getClay() > 20 && soil.getSand() < 60) score += 0.3;
            else score += 0.2;

            return score;

        } catch (Exception e) {
            log.warn("Impossible de récupérer le sol pour terrain ID {}, simulation...", terrain.getId());
            return 0.5;
        }
    }

    // =====================================================
    // 🌦 COMPATIBILITÉ CLIMAT
    // =====================================================
    private double calculateClimateCompatibility(TerrainAgricole terrain) {
        if (terrain.getDonneesClimatiques() == null || terrain.getDonneesClimatiques().isEmpty())
            return 0.5;

        double avgRain = terrain.getDonneesClimatiques()
                .stream()
                .filter(d -> d != null)
                .mapToDouble(d -> d.getPluviometrie())
                .average()
                .orElse(0);

        if (avgRain >= 40) return 0.8;
        if (avgRain >= 25) return 0.6;
        return 0.4;
    }

    // =====================================================
    // 🔧 UTILITAIRE ARRONDI
    // =====================================================
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}