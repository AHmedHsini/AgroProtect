package tn.esprit.scoringaideservice.service;

import org.springframework.stereotype.Component;

@Component
public class ScoringCalculator {

    public double calculateFinalScore(
            double agronomic,
            double climate,
            double productivity,
            double stability,
            double market
    ) {
        double score =
                (agronomic * 0.20) +
                (climate * 0.20) +
                (productivity * 0.25) +
                (stability * 0.15) +
                (market * 0.20);

        return Math.max(0, Math.min(score, 100));
    }
}

