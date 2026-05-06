package tn.esprit.scoringaideservice.service;

import tn.esprit.scoringaideservice.dto.MarketCommodityDTO;
import tn.esprit.scoringaideservice.dto.CropRecommendationDTO;

import java.util.List;

public interface MarketLocalService {

    /**
     * Retourne la liste des produits stratégiques
     * avec leur dépendance aux importations.
     */
    List<MarketCommodityDTO> getMarketDependency();

    /**
     * Retourne les meilleures opportunités de culture
     * pour un terrain donné.
     */
    List<CropRecommendationDTO> getOpportunities(Long terrainId);
    double calculateMarketScoreForCrop(String cropName);
    double calculateMarketScore(Long terrainId);
}