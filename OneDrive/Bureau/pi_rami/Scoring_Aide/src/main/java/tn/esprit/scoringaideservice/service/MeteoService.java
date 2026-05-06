package tn.esprit.scoringaideservice.service;

import tn.esprit.scoringaideservice.dto.MeteoDTO;
import java.util.List;
import tn.esprit.scoringaideservice.dto.MeteoHistoriqueDTO;

public interface MeteoService {

    MeteoDTO getMeteoByTerrain(Long terrainId);



    List<MeteoHistoriqueDTO> getHistorique(Long terrainId);

}
