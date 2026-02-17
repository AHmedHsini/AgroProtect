package tn.esprit.scoringaideservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.scoringaideservice.dto.*;
import tn.esprit.scoringaideservice.service.SatelliteService;

import java.util.List;

@RestController
@RequestMapping("/api/satellite")
@RequiredArgsConstructor
public class SatelliteController {

    private final SatelliteService satelliteService;

    @GetMapping("/ndvi/{terrainId}")
    public SatelliteIndexDTO getIndices(@PathVariable Long terrainId) {
        return satelliteService.getIndices(terrainId);
    }

    @GetMapping("/evolution/{terrainId}")
    public List<SatelliteEvolutionDTO> getEvolution(@PathVariable Long terrainId) {
        return satelliteService.getEvolution(terrainId);
    }

    @GetMapping("/biomasse/{terrainId}")
    public SatelliteBiomasseDTO getBiomasse(@PathVariable Long terrainId) {
        return satelliteService.getBiomasse(terrainId);
    }
}
