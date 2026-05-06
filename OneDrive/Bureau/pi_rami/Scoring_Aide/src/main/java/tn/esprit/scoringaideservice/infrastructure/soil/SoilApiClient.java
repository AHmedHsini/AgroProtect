package tn.esprit.scoringaideservice.infrastructure.soil;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tn.esprit.scoringaideservice.infrastructure.soil.SoilClassificationResponse;


@Component
public class SoilApiClient {

    private final WebClient webClient;

    public SoilApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public SoilClassificationResponse fetchClassification(double lat, double lon) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("rest.isric.org")
                        .path("/soilgrids/v2.0/classification/query")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .build())
                .retrieve()
                .bodyToMono(SoilClassificationResponse.class)
                .block();
    }


    public String fetchProperties(double lat, double lon) {

        return webClient.get()
                .uri("https://rest.isric.org/soilgrids/v2.0/properties/query"
                        + "?lat=" + lat
                        + "&lon=" + lon
                        + "&property=phh2o"
                        + "&property=ocd"
                        + "&property=clay"
                        + "&property=sand"
                        + "&depth=0-5cm")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }




}
