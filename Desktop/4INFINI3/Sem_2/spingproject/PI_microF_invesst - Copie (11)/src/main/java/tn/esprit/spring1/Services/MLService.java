package tn.esprit.spring1.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.Project;
import org.springframework.web.reactive.function.client.WebClient;
import tn.esprit.spring1.entities.Project;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MLService {

    private final WebClient webClient = WebClient.create("http://localhost:8001");

    public double predict(Project project) {

        try {

            Map<String, Object> body = new HashMap<>();

            body.put("funding_goal", project.getFundingGoal());
            body.put("collected_amount", project.getCollectedAmount());
            body.put("avg_revenue", calculateAvgRevenue(project));
            body.put("expenses", calculateExpenses(project));
            body.put("duration_months", 12);

            Map res = webClient.post()
                    .uri("/predict")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(java.time.Duration.ofSeconds(5)); // 🔥 timeout

            if (res != null && res.containsKey("probability")) {
                return Double.parseDouble(res.get("probability").toString());
            }

        } catch (Exception e) {
            System.out.println("⚠️ ML ERROR: " + e.getMessage());
        }

        // 🔥 fallback intelligent (important pour éviter crash)
        return 0.5;
    }
    private double calculateExpenses(Project project) {

        if (project.getRevenues() == null || project.getRevenues().isEmpty()) {
            return 0;
        }

        return project.getRevenues().stream()
                .mapToDouble(r -> r.getExpenses())
                .sum();
    }

    private double calculateAvgRevenue(Project project) {

        if (project.getRevenues() == null || project.getRevenues().isEmpty()) {
            return 0;
        }

        return project.getRevenues().stream()
                .mapToDouble(r -> r.getRevenueAmount())
                .average()
                .orElse(0);
    }
}
