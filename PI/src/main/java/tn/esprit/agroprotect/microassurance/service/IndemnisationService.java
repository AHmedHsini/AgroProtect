package tn.esprit.agroprotect.microassurance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.agroprotect.microassurance.dto.request.CreateIndemnisationRequest;
import tn.esprit.agroprotect.microassurance.dto.request.PayIndemnisationRequest;
import tn.esprit.agroprotect.microassurance.dto.response.IndemnisationResponse;
import tn.esprit.agroprotect.microassurance.dto.mapper.IndemnisationMapper;
import tn.esprit.agroprotect.microassurance.entity.Indemnisation;
import tn.esprit.agroprotect.microassurance.entity.Sinistre;
import tn.esprit.agroprotect.microassurance.enums.StatutIndemnisation;
import tn.esprit.agroprotect.microassurance.exception.ConflictException;
import tn.esprit.agroprotect.microassurance.exception.ForbiddenException;
import tn.esprit.agroprotect.microassurance.exception.NotFoundException;
import tn.esprit.agroprotect.microassurance.repository.IndemnisationRepository;
import tn.esprit.agroprotect.microassurance.repository.SinistreRepository;
import tn.esprit.agroprotect.microassurance.security.SecurityUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Service pour la gestion des indemnisations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IndemnisationService {

    private final IndemnisationRepository indemnisationRepository;
    private final SinistreRepository sinistreRepository;
    private final IndemnisationMapper indemnisationMapper;
    private final SecurityUtil securityUtil;

    /**
     * Crée une nouvelle indemnisation pour un sinistre
     */
    public IndemnisationResponse createIndemnisation(Long sinistreId, CreateIndemnisationRequest request) {
        if (!securityUtil.canCreateIndemnisation()) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation de créer des indemnisations");
        }

        Sinistre sinistre = findSinistreById(sinistreId);

        if (!sinistre.canCreateIndemnisation()) {
            throw new ConflictException("Une indemnisation ne peut pas être créée pour ce sinistre. " +
                    "Le sinistre doit être validé et ne pas avoir d'indemnisation existante.");
        }

        // Calcul du montant si pas fourni
        BigDecimal montant = request.getMontant();
        if (montant == null) {
            montant = calculateIndemnisationAmount(sinistre);
            if (montant == null) {
                throw new ConflictException("Le montant d'indemnisation doit être fourni car il ne peut pas être calculé automatiquement");
            }
        }

        Indemnisation indemnisation = new Indemnisation();
        indemnisation.setSinistre(sinistre);
        indemnisation.setMontant(montant);
        indemnisation.setStatut(StatutIndemnisation.EN_ATTENTE);

        Indemnisation savedIndemnisation = indemnisationRepository.save(indemnisation);

        log.info("Indemnisation créée avec l'ID {} pour le sinistre {} par l'utilisateur {}", 
                savedIndemnisation.getId(), sinistreId, securityUtil.getCurrentUserId());

        return indemnisationMapper.toResponse(savedIndemnisation);
    }

    /**
     * Récupère une indemnisation par son ID
     */
    @Transactional(readOnly = true)
    public IndemnisationResponse getIndemnisationById(Long id) {
        Indemnisation indemnisation = findIndemnisationById(id);
        
        // Vérification des permissions
        if (!securityUtil.canViewAllSinistres() && 
            !securityUtil.isOwnerOf(indemnisation.getSinistre().getCreatedByUserId())) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation de voir cette indemnisation");
        }

        return indemnisationMapper.toResponse(indemnisation);
    }

    /**
     * Recherche des indemnisations avec filtres
     */
    @Transactional(readOnly = true)
    public Page<IndemnisationResponse> searchIndemnisations(
            StatutIndemnisation statut,
            Long sinistreId,
            Long createdByUserId,
            Instant dateFrom,
            Instant dateTo,
            Pageable pageable) {

        Long filterUserId = null;
        
        // Si l'utilisateur n'est pas admin/expert, il ne voit que ses propres indemnisations
        if (!securityUtil.canViewAllSinistres()) {
            filterUserId = securityUtil.getCurrentUserId();
        } else if (createdByUserId != null) {
            filterUserId = createdByUserId;
        }

        Page<Indemnisation> indemnisations = indemnisationRepository.findWithFilters(
                statut,
                sinistreId,
                filterUserId,
                dateFrom,
                dateTo,
                pageable);

        return indemnisations.map(indemnisationMapper::toResponse);
    }

    /**
     * Effectue le paiement d'une indemnisation
     */
    public IndemnisationResponse payIndemnisation(Long id, PayIndemnisationRequest request, String idempotencyKey) {
        if (!securityUtil.canProcessPayments()) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation d'effectuer des paiements");
        }

        // Vérification de l'idempotence
        if (idempotencyKey != null) {
            Optional<Indemnisation> existingIndemnisation = indemnisationRepository.findByIdempotencyKey(idempotencyKey);
            if (existingIndemnisation.isPresent()) {
                log.info("Requête idempotente détectée pour la clé: {}", idempotencyKey);
                return indemnisationMapper.toResponse(existingIndemnisation.get());
            }
        }

        Indemnisation indemnisation = findIndemnisationById(id);

        if (!indemnisation.canBePaid()) {
            throw new ConflictException("Cette indemnisation ne peut pas être payée. Statut actuel: " + indemnisation.getStatut());
        }

        // Effectuer le paiement
        String paymentRef = request.getPaymentReference();
        if (paymentRef == null || paymentRef.trim().isEmpty()) {
            paymentRef = generatePaymentReference(indemnisation);
        }

        indemnisation.markAsPaid(paymentRef);
        if (idempotencyKey != null) {
            indemnisation.setIdempotencyKey(idempotencyKey);
        }

        Indemnisation savedIndemnisation = indemnisationRepository.save(indemnisation);

        log.info("Indemnisation {} payée par l'utilisateur {} avec la référence: {}", 
                id, securityUtil.getCurrentUserId(), paymentRef);

        return indemnisationMapper.toResponse(savedIndemnisation);
    }

    /**
     * Annule une indemnisation
     */
    public IndemnisationResponse cancelIndemnisation(Long id) {
        if (!securityUtil.canProcessPayments()) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation d'annuler des indemnisations");
        }

        Indemnisation indemnisation = findIndemnisationById(id);

        if (indemnisation.getStatut() != StatutIndemnisation.EN_ATTENTE) {
            throw new ConflictException("Seules les indemnisations en attente peuvent être annulées");
        }

        indemnisation.cancel();
        Indemnisation savedIndemnisation = indemnisationRepository.save(indemnisation);

        log.info("Indemnisation {} annulée par l'utilisateur {}", id, securityUtil.getCurrentUserId());

        return indemnisationMapper.toResponse(savedIndemnisation);
    }

    /**
     * Calcule le montant d'indemnisation basé sur l'estimation de perte et le taux de remboursement
     */
    private BigDecimal calculateIndemnisationAmount(Sinistre sinistre) {
        BigDecimal estimationPerte = sinistre.getEstimationPerte();
        BigDecimal tauxRemboursement = sinistre.getTauxRemboursement();

        if (estimationPerte != null && tauxRemboursement != null) {
            return estimationPerte.multiply(tauxRemboursement);
        }

        return null;
    }

    /**
     * Génère une référence de paiement automatique
     */
    private String generatePaymentReference(Indemnisation indemnisation) {
        return String.format("PAY-%d-%d", indemnisation.getSinistre().getId(), System.currentTimeMillis() % 100000);
    }

    /**
     * Trouve un sinistre par son ID ou lève une exception
     */
    private Sinistre findSinistreById(Long id) {
        return sinistreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sinistre non trouvé avec l'ID: " + id));
    }

    /**
     * Trouve une indemnisation par son ID ou lève une exception
     */
    private Indemnisation findIndemnisationById(Long id) {
        return indemnisationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Indemnisation non trouvée avec l'ID: " + id));
    }
}