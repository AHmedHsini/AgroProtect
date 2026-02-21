package tn.esprit.agroprotect.microassurance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.esprit.agroprotect.microassurance.enums.StatutIndemnisation;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entité représentant une indemnisation (paiement)
 */
@Entity
@Table(name = "indemnisations", indexes = {
    @Index(name = "idx_sinistre_id", columnList = "sinistreId", unique = true),
    @Index(name = "idx_statut_indemnisation", columnList = "statut"),
    @Index(name = "idx_date_creation_indemnisation", columnList = "dateCreation")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class Indemnisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistreId", nullable = false, unique = true)
    private Sinistre sinistre;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private Instant dateCreation;

    private Instant datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutIndemnisation statut = StatutIndemnisation.EN_ATTENTE;

    @Column(length = 100)
    private String paymentReference;

    @Column(length = 50)
    private String idempotencyKey;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public Indemnisation() {
        this.dateCreation = Instant.now();
    }

    /**
     * Vérifie si l'indemnisation peut être payée
     */
    public boolean canBePaid() {
        return statut == StatutIndemnisation.EN_ATTENTE;
    }

    /**
     * Marque l'indemnisation comme payée
     */
    public void markAsPaid(String paymentRef) {
        this.statut = StatutIndemnisation.PAYE;
        this.datePaiement = Instant.now();
        this.paymentReference = paymentRef;
    }

    /**
     * Annule l'indemnisation
     */
    public void cancel() {
        this.statut = StatutIndemnisation.ANNULE;
    }
}