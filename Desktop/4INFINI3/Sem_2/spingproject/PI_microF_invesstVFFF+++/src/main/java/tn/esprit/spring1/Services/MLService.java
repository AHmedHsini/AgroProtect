package tn.esprit.spring1.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.dto.MLResponse;
import tn.esprit.spring1.entities.Project;
import org.springframework.web.reactive.function.client.WebClient;
import tn.esprit.spring1.entities.Project;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MLService {

    private final WebClient webClient = WebClient.create("http://localhost:8001");

    public MLResponse predictWithExplanation(Project project) {

        try {

            Map<String, Object> body = new HashMap<>();

            body.put("funding_goal", project.getFundingGoal());
            body.put("collected_amount", project.getCollectedAmount());
            body.put("avg_revenue", calculateAvgRevenue(project));
            body.put("expenses", calculateExpenses(project));
            body.put("duration_months", 12);

            MLResponse res = webClient.post()
                    .uri("/predict")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(MLResponse.class)
                    .block(java.time.Duration.ofSeconds(5));

            if (res != null) {
                return res;
            }

        } catch (Exception e) {
            System.out.println("⚠️ ML ERROR: " + e.getMessage());
        }

        // 🔥 fallback propre
        MLResponse fallback = new MLResponse();
        fallback.setProbability(0.5);
        return fallback;
    }

    public double predict(Project project) {
        return predictWithExplanation(project).getProbability();
    }

    public String buildExplanation(Map<String, Double> exp) {

        if (exp == null) return "No AI explanation available";

        return "AI based on: "
                + "Collected (" + percent(exp.get("collected_amount")) + "), "
                + "Revenue (" + percent(exp.get("avg_revenue")) + "), "
                + "Expenses (" + percent(exp.get("expenses")) + ")";
    }

    private String percent(Double v) {
        if (v == null) return "0%";
        return String.format("%.0f%%", v * 100);
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
