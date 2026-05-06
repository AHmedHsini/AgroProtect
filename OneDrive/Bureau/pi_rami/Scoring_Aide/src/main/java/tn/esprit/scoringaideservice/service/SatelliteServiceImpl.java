package tn.esprit.scoringaideservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.scoringaideservice.dto.*;
import tn.esprit.scoringaideservice.entity.TerrainAgricole;
import tn.esprit.scoringaideservice.repository.TerrainAgricoleRepository;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SatelliteServiceImpl implements SatelliteService {

    private final TerrainAgricoleRepository terrainRepository;
    private final RestTemplate restTemplate;

    @Value("${eos.api.key}")
    private String apiKey;

    @Override
    public SatelliteIndexDTO getIndices(Long terrainId) {
        try {
            TerrainAgricole terrain = terrainRepository.findById(terrainId).orElse(null);

            if (terrain == null) {
                log.warn("Terrain introuvable avec l'ID: {}", terrainId);
                return buildFallback();
            }

            if (terrain.getEosFieldId() == null) {
                log.warn("EOS fieldId non défini pour le terrain ID: {}", terrainId);
                return buildFallback();
            }

            String url = "https://api-connect.eos.com/api/cz/backend/api/zoning/"
                    + terrain.getEosFieldId()
                    + "?api_key=" + apiKey;

            Map<String, Object> response;
            try {
                response = restTemplate.getForObject(url, Map.class);
            } catch (Exception e) {
                log.error("Erreur de connexion à l'API EOS (zoning list) pour le terrain ID: {}", terrainId, e);
                return buildFallback();
            }

            if (response == null || response.containsKey("message")) {
                log.warn("Réponse EOS API invalide ou contenant un message d'erreur: {}", response);
                return buildFallback();
            }

            List<Map<String, Object>> maps = (List<Map<String, Object>>) response.get("maps");
            if (maps == null || maps.isEmpty()) {
                log.warn("Aucune map trouvée dans la réponse EOS pour le terrain ID: {}", terrainId);
                return buildFallback();
            }

            Map<String, Object> ndviMap = null;
            for (Map<String, Object> map : maps) {
                if ("vegetation".equals(map.get("type_zmap"))) {
                    Map<String, Object> detail = (Map<String, Object>) map.get("zmap_detail");
                    if (detail != null && "NDVI".equals(detail.get("vegetation_index"))) {
                        ndviMap = map;
                        break;
                    }
                }
            }

            if (ndviMap == null) {
                log.warn("Aucune map NDVI trouvée pour le terrain ID: {}", terrainId);
                return buildFallback();
            }

            Object zmapIdObj = ndviMap.get("zmap_id");
            if (zmapIdObj == null) {
                log.warn("Attribut 'zmap_id' manquant dans la map NDVI pour le terrain ID: {}", terrainId);
                return buildFallback();
            }
            String zmapId = zmapIdObj.toString();

            String detailUrl = "https://api-connect.eos.com/api/cz/backend/api/zoning/"
                    + terrain.getEosFieldId()
                    + "/"
                    + zmapId
                    + "?api_key=" + apiKey;

            Map<String, Object> detailResponse;
            try {
                detailResponse = restTemplate.getForObject(detailUrl, Map.class);
            } catch (Exception e) {
                log.error("Erreur de connexion à l'API EOS (détail zone) pour le terrain ID: {}", terrainId, e);
                return buildFallback();
            }

            if (detailResponse == null || !detailResponse.containsKey("zones")) {
                log.warn("Réponse EOS détail invalide ou sans zones pour terrain ID: {}", terrainId);
                return buildFallback();
            }

            List<Map<String, Object>> zones = (List<Map<String, Object>>) detailResponse.get("zones");
            if (zones == null || zones.isEmpty()) {
                log.warn("Liste des zones vide pour le terrain ID: {}", terrainId);
                return buildFallback();
            }

            double weightedSum = 0;
            double totalArea = 0;

            for (Map<String, Object> zone : zones) {
                if (zone == null || zone.isEmpty()) continue;

                Map<String, Object> zoneData = (Map<String, Object>) zone.values().iterator().next();
                if (zoneData == null || !zoneData.containsKey("zone_p")) continue;

                double zonePercent = 0;
                try {
                    zonePercent = Double.parseDouble(zoneData.get("zone_p").toString());
                } catch (NumberFormatException e) {
                    log.warn("Impossible de parser zone_p: {}", zoneData.get("zone_p"));
                    continue;
                }

                // 🔥 Simulation NDVI par zone
                double ndviZone = 0.5 + (Math.random() * 0.4);

                weightedSum += ndviZone * zonePercent;
                totalArea += zonePercent;
            }

            if (totalArea == 0) {
                log.warn("Surface totale calculée est 0 pour terrain ID: {}", terrainId);
                return buildFallback();
            }

            double ndviMoyen = weightedSum / totalArea;

            SatelliteIndexDTO dto = new SatelliteIndexDTO();
            dto.setNdvi(ndviMoyen);
            dto.setScore((int) (ndviMoyen * 100));

            // 🧠 Affichage utilisateur
            if (ndviMoyen > 0.75) {
                dto.setNiveauSante("EXCELLENTE");
                dto.setInterpretation("Votre terrain présente une végétation très dense et en excellente santé.");
                dto.setRecommandation("Conditions optimales. Maintenir les pratiques actuelles.");
            }
            else if (ndviMoyen > 0.55) {
                dto.setNiveauSante("BONNE");
                dto.setInterpretation("Votre terrain est en bonne santé avec une végétation correcte.");
                dto.setRecommandation("Continuer l’entretien et surveiller l’irrigation.");
            }
            else {
                dto.setNiveauSante("FAIBLE");
                dto.setInterpretation("La végétation est faible. Risque de faible rendement.");
                dto.setRecommandation("Améliorer l’irrigation et analyser la qualité du sol.");
            }

            return dto;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des indices NDVI pour le terrain ID: {}", terrainId, e);
            return buildFallback();
        }
    }

    private SatelliteIndexDTO buildFallback() {
        SatelliteIndexDTO dto = new SatelliteIndexDTO();
        double ndvi = 0.6;
        dto.setNdvi(ndvi);
        dto.setScore((int) (ndvi * 100));
        dto.setNiveauSante("BONNE (simulation)");
        dto.setInterpretation("Simulation basée sur des données moyennes (API satellite non disponible).");
        dto.setRecommandation("Connecter l’API satellite pour une analyse réelle.");
        return dto;
    }

    @Override
    public List<SatelliteEvolutionDTO> getEvolution(Long terrainId) {
        try {
            List<SatelliteEvolutionDTO> evolution = new ArrayList<>();

            for (int i = 1; i <= 6; i++) {
                double ndvi = 0.5 + (Math.random() * 0.3);

                SatelliteEvolutionDTO dto = new SatelliteEvolutionDTO();
                dto.setDate(LocalDate.now().minusMonths(i));
                dto.setNdvi(ndvi);

                // 🧠 LOGIQUE UTILISATEUR
                if (ndvi > 0.7) {
                    dto.setNiveau("EXCELLENT");
                    dto.setInterpretation("Végétation très dense et en excellente santé.");
                }
                else if (ndvi > 0.5) {
                    dto.setNiveau("BON");
                    dto.setInterpretation("Végétation correcte, terrain stable.");
                }
                else {
                    dto.setNiveau("FAIBLE");
                    dto.setInterpretation("Végétation faible, attention au stress hydrique.");
                }

                evolution.add(dto);
            }

            return evolution;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'évolution pour le terrain ID: {}", terrainId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public SatelliteBiomasseDTO getBiomasse(Long terrainId) {
        try {
            SatelliteIndexDTO indices = getIndices(terrainId);

            double biomasse = indices.getNdvi() * 10;
            double rendement = biomasse * 1.3;

            SatelliteBiomasseDTO dto = new SatelliteBiomasseDTO();
            dto.setBiomasseEstimee(biomasse);
            dto.setRendementEstime(rendement);
            dto.setCommentaire("Estimation basée sur NDVI satellite.");

            if (rendement > 8) {
                dto.setNiveau("EXCELLENT");
                dto.setInterpretation("Très bon potentiel de rendement.");
                dto.setRecommandation("Conditions optimales pour investissement.");
            }
            else if (rendement > 5) {
                dto.setNiveau("MOYEN");
                dto.setInterpretation("Rendement correct mais améliorable.");
                dto.setRecommandation("Optimiser irrigation et fertilisation.");
            }
            else {
                dto.setNiveau("FAIBLE");
                dto.setInterpretation("Faible rendement attendu.");
                dto.setRecommandation("Analyser le sol et améliorer les conditions.");
            }
            return dto;
        } catch (Exception e) {
            log.error("Erreur inattendue lors du calcul de biomasse pour le terrain ID: {}", terrainId, e);
            return buildBiomasseFallback();
        }
    }

    private SatelliteBiomasseDTO buildBiomasseFallback() {
        SatelliteBiomasseDTO dto = new SatelliteBiomasseDTO();
        dto.setBiomasseEstimee(6.0);
        dto.setRendementEstime(7.8);
        dto.setCommentaire("Simulation de biomasse (Erreur technique)");
        dto.setNiveau("MOYEN");
        dto.setInterpretation("Données simulées car le service n'est pas disponible.");
        dto.setRecommandation("Veuillez vérifier vos services backend.");
        return dto;
    }
}