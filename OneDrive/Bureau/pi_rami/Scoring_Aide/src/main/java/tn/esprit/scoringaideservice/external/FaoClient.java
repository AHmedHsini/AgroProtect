package tn.esprit.scoringaideservice.external;

import org.springframework.stereotype.Component;
import tn.esprit.scoringaideservice.dto.MarketCommodityDTO;

import java.util.ArrayList;
import java.util.List;

@Component
public class FaoClient {

    public List<MarketCommodityDTO> fetchTunisiaMarketData() {

        List<MarketCommodityDTO> list = new ArrayList<>();

        list.add(build("Blé tendre", 800000, 1200000, 50000));
        list.add(build("Maïs", 200000, 900000, 20000));
        list.add(build("Lentilles", 50000, 150000, 5000));
        list.add(build("Pois chiches", 70000, 180000, 8000));
        list.add(build("Orge", 600000, 400000, 20000));

        return list;
    }

    private MarketCommodityDTO build(
            String name,
            double production,
            double imports,
            double exports) {

        double ratio = imports / (production + imports);

        return new MarketCommodityDTO(
                name,
                production,
                imports,
                exports,
                ratio
        );
    }
}