package com.agriplatform.identity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Client for ML Face Recognition Service (Python FastAPI).
 * 
 * Communicates with external face recognition microservice for:
 * - Face embedding extraction
 * - Liveness detection
 * - Face comparison
 */
@Service
@Slf4j
public class MlFaceServiceClient {

    private final WebClient webClient;

    public MlFaceServiceClient(
            @Value("${ml.service.url:http://localhost:8001}") String mlServiceUrl,
            @Value("${ml.service.api-key:}") String apiKey) {

        this.webClient = WebClient.builder()
                .baseUrl(mlServiceUrl)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Extract face embedding and perform liveness detection.
     */
    public FaceExtractionResult extractFaceEmbedding(String base64Image) {
        try {
            Map<String, Object> request = Map.of(
                    "image", base64Image,
                    "detect_liveness", true);

            ExtractionResponse response = webClient.post()
                    .uri("/api/v1/face/extract")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ExtractionResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response == null || !response.success()) {
                String error = response != null ? response.error() : "No response from ML service";
                log.error("Face extraction failed: {}", error);
                return FaceExtractionResult.failure(error);
            }

            return FaceExtractionResult.success(
                    response.embedding(),
                    response.livenessScore(),
                    response.qualityScore());

        } catch (Exception e) {
            log.error("ML service error during face extraction", e);
            return FaceExtractionResult.failure("ML service unavailable: " + e.getMessage());
        }
    }

    /**
     * Compare two face embeddings.
     */
    public FaceComparisonResult compareFaces(String embedding1, String embedding2) {
        try {
            Map<String, Object> request = Map.of(
                    "embedding1", embedding1,
                    "embedding2", embedding2);

            ComparisonResponse response = webClient.post()
                    .uri("/api/v1/face/compare")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ComparisonResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.success()) {
                return FaceComparisonResult.failure("Comparison failed");
            }

            return FaceComparisonResult.success(response.similarity());

        } catch (Exception e) {
            log.error("ML service error during face comparison", e);
            return FaceComparisonResult.failure("ML service unavailable");
        }
    }

    /**
     * Health check for ML service.
     */
    public boolean isHealthy() {
        try {
            return Boolean.TRUE.equals(webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(m -> "ok".equals(m.get("status")))
                    .timeout(Duration.ofSeconds(5))
                    .onErrorReturn(false)
                    .block());
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Response Types ====================

    public record FaceExtractionResult(
            boolean success,
            String embedding,
            double livenessScore,
            double qualityScore,
            String errorMessage) {
        public static FaceExtractionResult success(String embedding, double liveness, double quality) {
            return new FaceExtractionResult(true, embedding, liveness, quality, null);
        }

        public static FaceExtractionResult failure(String error) {
            return new FaceExtractionResult(false, null, 0, 0, error);
        }
    }

    public record FaceComparisonResult(
            boolean success,
            double similarity,
            String errorMessage) {
        public static FaceComparisonResult success(double similarity) {
            return new FaceComparisonResult(true, similarity, null);
        }

        public static FaceComparisonResult failure(String error) {
            return new FaceComparisonResult(false, 0, error);
        }
    }

    // Internal response DTOs for ML service
    private record ExtractionResponse(
            boolean success,
            String embedding,
            double livenessScore,
            double qualityScore,
            String error) {
    }

    private record ComparisonResponse(
            boolean success,
            double similarity,
            String error) {
    }
}
