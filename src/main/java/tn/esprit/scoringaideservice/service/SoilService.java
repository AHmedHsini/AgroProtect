package tn.esprit.scoringaideservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import tn.esprit.scoringaideservice.entity.TerrainAgricole;
import tn.esprit.scoringaideservice.repository.TerrainAgricoleRepository;
import tn.esprit.scoringaideservice.infrastructure.soil.SoilClassificationResponse;
import tn.esprit.scoringaideservice.infrastructure.soil.SoilApiClient;
import tn.esprit.scoringaideservice.dto.SoilData;

@Service
public class SoilService {

    private final SoilApiClient soilApiClient;
    private final TerrainAgricoleRepository terrainRepository;
    private final ObjectMapper objectMapper;

    public SoilService(SoilApiClient soilApiClient,
                       TerrainAgricoleRepository terrainRepository) {
        this.soilApiClient = soilApiClient;
        this.terrainRepository = terrainRepository;
        this.objectMapper = new ObjectMapper();
    }

    public SoilData getSoilByTerrainId(Long terrainId) {

        TerrainAgricole terrain =
                terrainRepository.findById(terrainId)
                        .orElseThrow(() ->
                                new RuntimeException("Terrain not found"));

        double lat = terrain.getLatitude();
        double lon = terrain.getLongitude();

        // 1️⃣ Classification WRB
        SoilClassificationResponse classification =
                soilApiClient.fetchClassification(lat, lon);

        // 2️⃣ Propriétés chimiques
        String propertiesJson =
                soilApiClient.fetchProperties(lat, lon);

        SoilData soilData = new SoilData();

        if (classification != null) {
            soilData.setWrbClass(classification.getWrbClassName());
            soilData.setProbability(classification.getProbability());
        }

        boolean realDataFound = false;

        // 3️⃣ Parsing robuste
        try {

            JsonNode root = objectMapper.readTree(propertiesJson);

            JsonNode layers = root.path("properties").path("layers");

            if (layers != null && layers.isArray()) {

                for (JsonNode layer : layers) {

                    String name = layer.path("name").asText();
                    JsonNode depths = layer.path("depths");

                    if (depths.isArray() && depths.size() > 0) {

                        JsonNode firstDepth = depths.get(0);
                        JsonNode values = firstDepth.path("values");

                        JsonNode meanNode = values.get("mean");

                        if (meanNode != null && !meanNode.isNull()) {

                            double meanValue = meanNode.asDouble();
                            realDataFound = true;

                            switch (name) {
                                case "phh2o":
                                    soilData.setPh(meanValue / 10.0);
                                    break;

                                case "ocd":
                                    soilData.setOrganicCarbon(meanValue / 10.0);
                                    break;

                                case "clay":
                                    soilData.setClay(meanValue);
                                    break;

                                case "sand":
                                    soilData.setSand(meanValue);
                                    break;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4️⃣ Fallback intelligent basé sur WRB
        if (!realDataFound) {

            String wrb = soilData.getWrbClass();

            switch (wrb) {

                case "Calcisols":
                    soilData.setPh(7.8);
                    soilData.setOrganicCarbon(1.5);
                    soilData.setClay(30);
                    soilData.setSand(35);
                    soilData.setDataConfidence(0.65);
                    break;

                case "Leptosols":
                    soilData.setPh(7.2);
                    soilData.setOrganicCarbon(0.8);
                    soilData.setClay(20);
                    soilData.setSand(45);
                    soilData.setDataConfidence(0.55);
                    break;

                case "Vertisols":
                    soilData.setPh(7.5);
                    soilData.setOrganicCarbon(2.0);
                    soilData.setClay(45);
                    soilData.setSand(20);
                    soilData.setDataConfidence(0.7);
                    break;

                case "Cambisols":
                    soilData.setPh(6.8);
                    soilData.setOrganicCarbon(1.8);
                    soilData.setClay(28);
                    soilData.setSand(32);
                    soilData.setDataConfidence(0.75);
                    break;

                default:
                    soilData.setPh(7.0);
                    soilData.setOrganicCarbon(1.2);
                    soilData.setClay(25);
                    soilData.setSand(40);
                    soilData.setDataConfidence(0.6);
            }

        } else {
            soilData.setDataConfidence(0.9);
        }

        return soilData;
    }
}
