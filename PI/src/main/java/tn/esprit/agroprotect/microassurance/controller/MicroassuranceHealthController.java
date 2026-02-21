package tn.esprit.agroprotect.microassurance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Contrôleur pour les vérifications de santé du service
 */
@RestController
@RequestMapping("/api/v1/microassurance")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Vérifications de santé du service")
public class MicroassuranceHealthController {

    @GetMapping("/health")
    @Operation(summary = "Vérification de santé du service", 
               description = "Endpoint public pour vérifier l'état du service")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "microassurance-service",
            "timestamp", Instant.now(),
            "version", "1.0.0-SNAPSHOT"
        );
        return ResponseEntity.ok(response);
    }
}