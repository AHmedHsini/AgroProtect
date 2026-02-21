package tn.esprit.agroprotect.microassurance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.agroprotect.microassurance.entity.Sinistre;
import tn.esprit.agroprotect.microassurance.enums.StatutSinistre;
import tn.esprit.agroprotect.microassurance.enums.TypeSinistre;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour les sinistres
 */
@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    /**
     * Trouve les sinistres par utilisateur créateur
     */
    Page<Sinistre> findByCreatedByUserId(Long createdByUserId, Pageable pageable);

    /**
     * Trouve les sinistres par contrat d'assurance
     */
    Page<Sinistre> findByContratAssuranceId(Long contratAssuranceId, Pageable pageable);

    /**
     * Trouve les sinistres par statut
     */
    Page<Sinistre> findByStatut(StatutSinistre statut, Pageable pageable);

    /**
     * Trouve les sinistres par type
     */
    Page<Sinistre> findByTypeSinistre(TypeSinistre typeSinistre, Pageable pageable);

    /**
     * Trouve un sinistre par ID et utilisateur créateur (pour la sécurité)
     */
    Optional<Sinistre> findByIdAndCreatedByUserId(Long id, Long createdByUserId);

    /**
     * Recherche avancée avec filtres multiples
     */
    @Query("SELECT s FROM Sinistre s WHERE " +
           "(:statut IS NULL OR s.statut = :statut) AND " +
           "(:typeSinistre IS NULL OR s.typeSinistre = :typeSinistre) AND " +
           "(:contratAssuranceId IS NULL OR s.contratAssuranceId = :contratAssuranceId) AND " +
           "(:createdByUserId IS NULL OR s.createdByUserId = :createdByUserId) AND " +
           "(:dateFrom IS NULL OR s.dateDeclaration >= :dateFrom) AND " +
           "(:dateTo IS NULL OR s.dateDeclaration <= :dateTo)")
    Page<Sinistre> findWithFilters(
            @Param("statut") StatutSinistre statut,
            @Param("typeSinistre") TypeSinistre typeSinistre,
            @Param("contratAssuranceId") Long contratAssuranceId,
            @Param("createdByUserId") Long createdByUserId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            Pageable pageable);

    /**
     * Compte les sinistres par statut pour un utilisateur
     */
    @Query("SELECT COUNT(s) FROM Sinistre s WHERE s.createdByUserId = :userId AND s.statut = :statut")
    long countByUserAndStatut(@Param("userId") Long userId, @Param("statut") StatutSinistre statut);

    /**
     * Trouve les sinistres validés sans indemnisation
     */
    @Query("SELECT s FROM Sinistre s WHERE s.statut = 'VALIDE' AND s.indemnisation IS NULL")
    List<Sinistre> findValidSinistresWithoutIndemnisation();
}