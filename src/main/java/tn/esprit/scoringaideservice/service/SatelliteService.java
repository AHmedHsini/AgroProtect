package tn.esprit.scoringaideservice.service;

import tn.esprit.scoringaideservice.dto.*;

import java.util.List;

public interface SatelliteService {

    SatelliteIndexDTO getIndices(Long terrainId);

    List<SatelliteEvolutionDTO> getEvolution(Long terrainId);

    SatelliteBiomasseDTO getBiomasse(Long terrainId);
}
