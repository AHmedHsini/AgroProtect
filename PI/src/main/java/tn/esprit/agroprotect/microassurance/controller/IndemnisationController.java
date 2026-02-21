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
import tn.esprit.agroprotect.microassurance.dto.request.CreateIndemnisationRequest;
import tn.esprit.agroprotect.microassurance.dto.request.PayIndemnisationRequest;
import tn.esprit.agroprotect.microassurance.dto.response.IndemnisationResponse;
import tn.esprit.agroprotect.microassurance.enums.StatutIndemnisation;
import tn.esprit.agroprotect.microassurance.service.IndemnisationService;

import java.time.Instant;

/**
 * Contrôleur REST pour la gestion des indemnisations
 */
@RestController
@RequestMapping("/v1/microassurance")
@RequiredArgsConstructor
@Tag(name = "Indemnisations", description = "Gestion des indemnisations et paiements")
public class IndemnisationController {

    private final IndemnisationService indemnisationService;

    @PostMapping("/sinistres/{sinistreId}/indemnisations")
    @Operation(summary = "Créer une indemnisation pour un sinistre", 
               description = "Crée une nouvelle indemnisation pour un sinistre validé (admin uniquement)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IndemnisationResponse> createIndemnisation(
            @Parameter(description = "ID du sinistre") @PathVariable Long sinistreId,
            @Valid @RequestBody(required = false) CreateIndemnisationRequest request) {
        if (request == null) {
            request = new CreateIndemnisationRequest();
        }
        IndemnisationResponse response = indemnisationService.createIndemnisation(sinistreId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/indemnisations/{id}")
    @Operation(summary = "Récupérer une indemnisation par son ID", 
               description = "Récupère les détails d'une indemnisation spécifique")
    public ResponseEntity<IndemnisationResponse> getIndemnisation(
            @Parameter(description = "ID de l'indemnisation") @PathVariable Long id) {
        IndemnisationResponse response = indemnisationService.getIndemnisationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indemnisations")
    @Operation(summary = "Rechercher des indemnisations", 
               description = "Recherche des indemnisations avec filtres optionnels")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPERT')")
    public ResponseEntity<Page<IndemnisationResponse>> searchIndemnisations(
            @Parameter(description = "Statut de l'indemnisation") @RequestParam(required = false) StatutIndemnisation statut,
            @Parameter(description = "ID du sinistre") @RequestParam(required = false) Long sinistreId,
            @Parameter(description = "ID de l'utilisateur créateur du sinistre") @RequestParam(required = false) Long createdByUserId,
            @Parameter(description = "Date de début (ISO 8601)") @RequestParam(required = false) Instant from,
            @Parameter(description = "Date de fin (ISO 8601)") @RequestParam(required = false) Instant to,
            @Parameter(description = "Numéro de page (commence à 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Critère de tri") @RequestParam(defaultValue = "dateCreation") String sort,
            @Parameter(description = "Direction du tri") @RequestParam(defaultValue = "desc") String direction) {

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<IndemnisationResponse> response = indemnisationService.searchIndemnisations(
                statut, sinistreId, createdByUserId, from, to, pageable);
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/indemnisations/{id}/pay")
    @Operation(summary = "Effectuer le paiement d'une indemnisation", 
               description = "Marque une indemnisation comme payée (admin uniquement)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IndemnisationResponse> payIndemnisation(
            @Parameter(description = "ID de l'indemnisation") @PathVariable Long id,
            @Valid @RequestBody(required = false) PayIndemnisationRequest request,
            @Parameter(description = "Clé d'idempotence pour éviter les paiements en double") 
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (request == null) {
            request = new PayIndemnisationRequest();
        }
        IndemnisationResponse response = indemnisationService.payIndemnisation(id, request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/indemnisations/{id}/cancel")
    @Operation(summary = "Annuler une indemnisation", 
               description = "Annule une indemnisation en attente (admin uniquement)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IndemnisationResponse> cancelIndemnisation(
            @Parameter(description = "ID de l'indemnisation") @PathVariable Long id) {
        IndemnisationResponse response = indemnisationService.cancelIndemnisation(id);
        return ResponseEntity.ok(response);
    }
}