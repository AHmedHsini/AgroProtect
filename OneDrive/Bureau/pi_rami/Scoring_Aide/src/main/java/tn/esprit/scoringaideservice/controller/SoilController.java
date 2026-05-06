package tn.esprit.scoringaideservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import tn.esprit.scoringaideservice.dto.SoilData;
import tn.esprit.scoringaideservice.service.SoilService;

@RestController

public class SoilController {

    private final SoilService soilService;

    public SoilController(SoilService soilService) {
        this.soilService = soilService;
    }

    @GetMapping("/api/soil/terrain/{id}")
    public SoilData getSoilByTerrain(@PathVariable Long id) {
        return soilService.getSoilByTerrainId(id);
    }
}


