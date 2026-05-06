package tn.esprit.scoringaideservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MlService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Double getPrediction(Double agronomique,
                                Double climatique,
                                Double productivite,
                                Double stabilite,
                                Double market) {

        String url = "http://localhost:5000/predict";

        // Construire le JSON
        Map<String, Object> request = new HashMap<>();
        request.put("agronomique", agronomique);
        request.put("climatique", climatique);
        request.put("productivite", productivite);
        request.put("stabilite", stabilite);
        request.put("market", market);

        // Appel API Flask
        Map<String, Object> response =
                restTemplate.postForObject(url, request, Map.class);

        // Retourner score
        if (response != null && response.get("predictedScore") != null) {
            return Double.valueOf(response.get("predictedScore").toString());
        }

        throw new RuntimeException("Erreur ML: réponse vide");
    }
}