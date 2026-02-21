package tn.esprit.agroprotect.microassurance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.agroprotect.microassurance.dto.request.CreateSinistreRequest;
import tn.esprit.agroprotect.microassurance.dto.request.RefuseSinistreRequest;
import tn.esprit.agroprotect.microassurance.dto.request.ValidateSinistreRequest;
import tn.esprit.agroprotect.microassurance.dto.response.SinistreResponse;
import tn.esprit.agroprotect.microassurance.enums.StatutSinistre;
import tn.esprit.agroprotect.microassurance.enums.TypeSinistre;
import tn.esprit.agroprotect.microassurance.service.SinistreService;

import java.time.Instant;

/**
 * Contrôleur REST pour la gestion des sinistres
 */
@RestController
@RequestMapping("/v1/microassurance/sinistres")
@RequiredArgsConstructor
@Tag(name = "Sinistres", description = "Gestion des déclarations de sinistres")
public class SinistreController {

    private final SinistreService sinistreService;

    @PostMapping
    @Operation(summary = "Créer un nouveau sinistre", 
               description = "Permet à un utilisateur de créer un sinistre")
    //@PreAuthorize("hasRole('USER') or hasRole('EXPERT') or hasRole('ADMIN')")  // TEMPORARY: Disabled for testing
    public ResponseEntity<SinistreResponse> createSinistre(@Valid @RequestBody CreateSinistreRequest request) {
        SinistreResponse response = sinistreService.createSinistre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un sinistre par son ID", 
               description = "Récupère les détails d'un sinistre spécifique")
    public ResponseEntity<SinistreResponse> getSinistre(
            @Parameter(description = "ID du sinistre") @PathVariable Long id) {
        SinistreResponse response = sinistreService.getSinistreById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Rechercher des sinistres", 
               description = "Recherche des sinistres avec filtres optionnels")
    public ResponseEntity<Page<SinistreResponse>> searchSinistres(
            @Parameter(description = "Statut du sinistre") @RequestParam(required = false) StatutSinistre statut,
            @Parameter(description = "Type de sinistre") @RequestParam(required = false) TypeSinistre typeSinistre,
            @Parameter(description = "ID du contrat d'assurance") @RequestParam(required = false) Long contratAssuranceId,
            @Parameter(description = "ID de l'utilisateur créateur") @RequestParam(required = false) Long createdByUserId,
            @Parameter(description = "Date de début (ISO 8601)") @RequestParam(required = false) Instant from,
            @Parameter(description = "Date de fin (ISO 8601)") @RequestParam(required = false) Instant to,
            @Parameter(description = "Numéro de page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Critère de tri") @RequestParam(defaultValue = "dateDeclaration") String sort,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "desc") String direction) {

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<SinistreResponse> response = sinistreService.searchSinistres(
                statut, typeSinistre, contratAssuranceId, createdByUserId, from, to, pageable);
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start-evaluation")
    @Operation(summary = "Commencer l'évaluation d'un sinistre", 
               description = "Met un sinistre en état d'évaluation (expert/admin uniquement)")
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<SinistreResponse> startEvaluation(
            @Parameter(description = "ID du sinistre") @PathVariable Long id) {
        SinistreResponse response = sinistreService.startEvaluation(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/validate")
    @Operation(summary = "Valider un sinistre", 
               description = "Valide un sinistre en évaluation (expert/admin uniquement)")
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<SinistreResponse> validateSinistre(
            @Parameter(description = "ID du sinistre") @PathVariable Long id,
            @Valid @RequestBody(required = false) ValidateSinistreRequest request) {
        if (request == null) {
            request = new ValidateSinistreRequest();
        }
        SinistreResponse response = sinistreService.validateSinistre(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/refuse")
    @Operation(summary = "Refuser un sinistre", 
               description = "Refuse un sinistre en évaluation avec un motif (expert/admin uniquement)")
    @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<SinistreResponse> refuseSinistre(
            @Parameter(description = "ID du sinistre") @PathVariable Long id,
            @Valid @RequestBody RefuseSinistreRequest request) {
        SinistreResponse response = sinistreService.refuseSinistre(id, request);
        return ResponseEntity.ok(response);
    }
}