package tn.esprit.scoringaideservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.scoringaideservice.dto.MarketCommodityDTO;
import tn.esprit.scoringaideservice.dto.CropRecommendationDTO;
import tn.esprit.scoringaideservice.dto.CropScoreComparisonDTO;
import tn.esprit.scoringaideservice.service.MarketLocalService;

import java.util.List;

@RestController
@RequestMapping("/api/market/local")
@RequiredArgsConstructor
public class MarketLocalController {

    private final MarketLocalService marketLocalService;

    @GetMapping("/dependency")
    public List<MarketCommodityDTO> getDependency() {
        return marketLocalService.getMarketDependency();
    }

    @GetMapping("/opportunities/{terrainId}")
    public List<CropRecommendationDTO> getOpportunities(
            @PathVariable Long terrainId) {
        return marketLocalService.getOpportunities(terrainId);
    }
}