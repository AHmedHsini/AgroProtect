package tn.esprit.scoringaideservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.scoringaideservice.dto.MeteoDTO;
import tn.esprit.scoringaideservice.service.MeteoService;
import tn.esprit.scoringaideservice.dto.MeteoHistoriqueDTO ;
import java.util.List;

@RestController
@RequestMapping("/api/meteo")
@RequiredArgsConstructor
public class MeteoController {

    private final MeteoService meteoService;

    @GetMapping("/{terrainId}")
    public MeteoDTO getMeteo(@PathVariable Long terrainId) {
        return meteoService.getMeteoByTerrain(terrainId);
    }
    @GetMapping("/historique/{terrainId}")
    public List<MeteoHistoriqueDTO> getHistorique(@PathVariable Long terrainId) {
        return meteoService.getHistorique(terrainId);
    }

}
