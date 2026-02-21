package tn.esprit.agroprotect.microassurance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.agroprotect.microassurance.entity.Indemnisation;
import tn.esprit.agroprotect.microassurance.enums.StatutIndemnisation;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository pour les indemnisations
 */
@Repository
public interface IndemnisationRepository extends JpaRepository<Indemnisation, Long> {

    /**
     * Trouve une indemnisation par ID de sinistre
     */
    Optional<Indemnisation> findBySinistreId(Long sinistreId);

    /**
     * Trouve les indemnisations par statut
     */
    Page<Indemnisation> findByStatut(StatutIndemnisation statut, Pageable pageable);

    /**
     * Vérifie si une indemnisation existe pour un sinistre
     */
    boolean existsBySinistreId(Long sinistreId);

    /**
     * Trouve une indemnisation par clé d'idempotence
     */
    Optional<Indemnisation> findByIdempotencyKey(String idempotencyKey);

    /**
     * Recherche avancée avec filtres
     */
    @Query("SELECT i FROM Indemnisation i WHERE " +
           "(:statut IS NULL OR i.statut = :statut) AND " +
           "(:sinistreId IS NULL OR i.sinistre.id = :sinistreId) AND " +
           "(:createdByUserId IS NULL OR i.sinistre.createdByUserId = :createdByUserId) AND " +
           "(:dateFrom IS NULL OR i.dateCreation >= :dateFrom) AND " +
           "(:dateTo IS NULL OR i.dateCreation <= :dateTo)")
    Page<Indemnisation> findWithFilters(
            @Param("statut") StatutIndemnisation statut,
            @Param("sinistreId") Long sinistreId,
            @Param("createdByUserId") Long createdByUserId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            Pageable pageable);

    /**
     * Calcule le montant total payé pour un utilisateur
     */
    @Query("SELECT COALESCE(SUM(i.montant), 0) FROM Indemnisation i " +
           "WHERE i.sinistre.createdByUserId = :userId AND i.statut = 'PAYE'")
    Double getTotalPaidAmountForUser(@Param("userId") Long userId);

    /**
     * Trouve les indemnisations en attente de paiement
     */
    @Query("SELECT i FROM Indemnisation i WHERE i.statut = 'EN_ATTENTE' ORDER BY i.dateCreation ASC")
    Page<Indemnisation> findPendingPayments(Pageable pageable);
}