package tn.esprit.scoringaideservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.scoringaideservice.dto.MeteoDTO;
import tn.esprit.scoringaideservice.dto.MeteoHistoriqueDTO;
import tn.esprit.scoringaideservice.entity.DonneeClimatique;
import tn.esprit.scoringaideservice.entity.TerrainAgricole;
import tn.esprit.scoringaideservice.repository.DonneeClimatiqueRepository;
import tn.esprit.scoringaideservice.repository.TerrainAgricoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MeteoServiceImpl implements MeteoService {

    private final TerrainAgricoleRepository terrainRepository;
    private final DonneeClimatiqueRepository donneeClimatiqueRepository;
    private final RestTemplate restTemplate;

    @Value("${meteo.api.key}")
    private String apiKey;

    // =====================================================
    // ✅ 1️⃣ METEO ACTUELLE (OpenWeather)
    // =====================================================
    @Override
    public MeteoDTO getMeteoByTerrain(Long terrainId) {

        TerrainAgricole terrain = terrainRepository.findById(terrainId)
                .orElseThrow(() -> new RuntimeException("Terrain introuvable"));

        if (terrain.getLatitude() == null || terrain.getLongitude() == null) {
            throw new RuntimeException("Latitude ou Longitude non définie");
        }

        String url = "https://api.openweathermap.org/data/2.5/weather?lat="
                + terrain.getLatitude()
                + "&lon="
                + terrain.getLongitude()
                + "&appid="
                + apiKey
                + "&units=metric";

        Map response = restTemplate.getForObject(url, Map.class);

        if (response == null) {
            throw new RuntimeException("Erreur API météo");
        }

        Map main = (Map) response.get("main");
        Map wind = (Map) response.get("wind");
        Map clouds = (Map) response.get("clouds");
        Map weather = ((List<Map>) response.get("weather")).get(0);

        MeteoDTO dto = new MeteoDTO();

        dto.setVille(response.get("name").toString());
        dto.setTemperature(Double.valueOf(main.get("temp").toString()));
        dto.setTemperatureRessentie(Double.valueOf(main.get("feels_like").toString()));
        dto.setHumidity(Integer.valueOf(main.get("humidity").toString()));
        dto.setWindSpeed(Double.valueOf(wind.get("speed").toString()));
        dto.setCloudiness(Integer.valueOf(clouds.get("all").toString()));
        dto.setDescription(weather.get("description").toString());

        dto.setResume(
                "À " + dto.getVille() +
                        " il fait " + dto.getTemperature() + "°C " +
                        "(ressenti " + dto.getTemperatureRessentie() + "°C), " +
                        dto.getDescription() +
                        ", humidité " + dto.getHumidity() + "%."
        );

        return dto;
    }

    // =====================================================
    // ✅ 2️⃣ HISTORIQUE CLIMATIQUE (3 dernières années)
    // =====================================================
    @Override
    public List<MeteoHistoriqueDTO> getHistorique(Long terrainId) {

        if (!terrainRepository.existsById(terrainId)) {
            throw new RuntimeException("Terrain introuvable");
        }

        List<DonneeClimatique> donnees =
                donneeClimatiqueRepository
                        .findByTerrainAgricoleIdOrderByAnneeDesc(terrainId);

        if (donnees.isEmpty()) {
            throw new RuntimeException("Aucune donnée climatique trouvée");
        }

        List<MeteoHistoriqueDTO> resultat = new ArrayList<>();

        // 🔥 Limiter aux 3 dernières années
        int limite = Math.min(3, donnees.size());

        for (int i = 0; i < limite; i++) {

            DonneeClimatique d = donnees.get(i);

            MeteoHistoriqueDTO dto = new MeteoHistoriqueDTO();
            dto.setAnnee(d.getAnnee());
            dto.setTemperatureMoyenne(d.getTemperatureMoyenne());
            dto.setPluieTotale(d.getPluviometrie());

            resultat.add(dto);
        }

        return resultat;
    }
}
