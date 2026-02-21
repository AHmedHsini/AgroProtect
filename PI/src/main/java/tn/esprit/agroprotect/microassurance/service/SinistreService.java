package tn.esprit.agroprotect.microassurance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.agroprotect.microassurance.dto.request.CreateSinistreRequest;
import tn.esprit.agroprotect.microassurance.dto.request.RefuseSinistreRequest;
import tn.esprit.agroprotect.microassurance.dto.request.ValidateSinistreRequest;
import tn.esprit.agroprotect.microassurance.dto.response.SinistreResponse;
import tn.esprit.agroprotect.microassurance.dto.mapper.SinistreMapper;
import tn.esprit.agroprotect.microassurance.entity.Sinistre;
import tn.esprit.agroprotect.microassurance.enums.StatutSinistre;
import tn.esprit.agroprotect.microassurance.enums.TypeSinistre;
import tn.esprit.agroprotect.microassurance.exception.ConflictException;
import tn.esprit.agroprotect.microassurance.exception.ForbiddenException;
import tn.esprit.agroprotect.microassurance.exception.NotFoundException;
import tn.esprit.agroprotect.microassurance.repository.SinistreRepository;
import tn.esprit.agroprotect.microassurance.security.SecurityUtil;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Service pour la gestion des sinistres
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SinistreService {

    private final SinistreRepository sinistreRepository;
    private final SinistreMapper sinistreMapper;
    private final SecurityUtil securityUtil;

    /**
     * Crée un nouveau sinistre
     */
    public SinistreResponse createSinistre(CreateSinistreRequest request) {
        Long userId = securityUtil.getCurrentUserId();

        Sinistre sinistre = sinistreMapper.toEntity(request);
        sinistre.setDateDeclaration(Instant.now());  // Explicitly set date
        sinistre.setCreatedByUserId(userId);
        sinistre.setStatut(StatutSinistre.DECLARE);

        Sinistre savedSinistre = sinistreRepository.save(sinistre);
        log.info("Sinistre créé avec l'ID: {}", savedSinistre.getId());

        return sinistreMapper.toResponse(savedSinistre);
    }

    /**
     * Récupère un sinistre par son ID
     */
    @Transactional(readOnly = true)
    public SinistreResponse getSinistreById(Long id) {
        Sinistre sinistre = findSinistreById(id);
        
        // Vérification des permissions
        if (!securityUtil.canViewSinistre(sinistre.getCreatedByUserId())) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation de voir ce sinistre");
        }

        return sinistreMapper.toResponse(sinistre);
    }

    /**
     * Recherche des sinistres avec filtres
     */
    @Transactional(readOnly = true)
    public Page<SinistreResponse> searchSinistres(
            StatutSinistre statut,
            TypeSinistre typeSinistre,
            Long contratAssuranceId,
            Long createdByUserId,
            Instant dateFrom,
            Instant dateTo,
            Pageable pageable) {

        Long filterUserId = null;
        
        // Si l'utilisateur n'est pas admin/expert, il ne voit que ses propres sinistres
        if (!securityUtil.canViewAllSinistres()) {
            filterUserId = securityUtil.getCurrentUserId();
        } else if (createdByUserId != null) {
            filterUserId = createdByUserId;
        }

        Page<Sinistre> sinistres = sinistreRepository.findWithFilters(
                statut,
                typeSinistre,
                contratAssuranceId,
                filterUserId,
                dateFrom,
                dateTo,
                pageable);

        return sinistres.map(sinistreMapper::toResponse);
    }

    /**
     * Commence l'évaluation d'un sinistre
     */
    public SinistreResponse startEvaluation(Long id) {
        if (!securityUtil.canModifySinistreStatus()) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation de modifier le statut des sinistres");
        }

        Sinistre sinistre = findSinistreById(id);

        if (!sinistre.canStartEvaluation()) {
            throw new ConflictException("Ce sinistre ne peut pas être mis en évaluation. Statut actuel: " + sinistre.getStatut());
        }

        sinistre.setStatut(StatutSinistre.EN_EVALUATION);
        Sinistre savedSinistre = sinistreRepository.save(sinistre);

        log.info("Sinistre {} mis en évaluation par l'utilisateur {}", id, securityUtil.getCurrentUserId());

        return sinistreMapper.toResponse(savedSinistre);
    }

    /**
     * Valide un sinistre
     */
    public SinistreResponse validateSinistre(Long id, ValidateSinistreRequest request) {
        if (!securityUtil.canModifySinistreStatus()) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation de valider les sinistres");
        }

        Sinistre sinistre = findSinistreById(id);

        if (!sinistre.canBeValidated()) {
            throw new ConflictException("Ce sinistre ne peut pas être validé. Statut actuel: " + sinistre.getStatut());
        }

        sinistre.setStatut(StatutSinistre.VALIDE);
        sinistre.setMotifRefus(null); // Clear any previous refusal reason
        
        if (request.getTauxRemboursement() != null) {
            sinistre.setTauxRemboursement(request.getTauxRemboursement());
        }

        Sinistre savedSinistre = sinistreRepository.save(sinistre);

        log.info("Sinistre {} validé par l'utilisateur {}", id, securityUtil.getCurrentUserId());

        return sinistreMapper.toResponse(savedSinistre);
    }

    /**
     * Refuse un sinistre
     */
    public SinistreResponse refuseSinistre(Long id, RefuseSinistreRequest request) {
        if (!securityUtil.canModifySinistreStatus()) {
            throw new ForbiddenException("Vous n'avez pas l'autorisation de refuser les sinistres");
        }

        Sinistre sinistre = findSinistreById(id);

        if (!sinistre.canBeRefused()) {
            throw new ConflictException("Ce sinistre ne peut pas être refusé. Statut actuel: " + sinistre.getStatut());
        }

        sinistre.setStatut(StatutSinistre.REFUSE);
        sinistre.setMotifRefus(request.getMotifRefus());
        sinistre.setTauxRemboursement(null); // Clear reimbursement rate

        Sinistre savedSinistre = sinistreRepository.save(sinistre);

        log.info("Sinistre {} refusé par l'utilisateur {} avec le motif: {}", 
                id, securityUtil.getCurrentUserId(), request.getMotifRefus());

        return sinistreMapper.toResponse(savedSinistre);
    }

    /**
     * Trouve un sinistre par son ID ou lève une exception
     */
    private Sinistre findSinistreById(Long id) {
        return sinistreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sinistre non trouvé avec l'ID: " + id));
    }
}