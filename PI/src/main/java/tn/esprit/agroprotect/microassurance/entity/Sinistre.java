package tn.esprit.agroprotect.microassurance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.esprit.agroprotect.microassurance.enums.StatutSinistre;
import tn.esprit.agroprotect.microassurance.enums.TypeSinistre;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entité représentant un sinistre (réclamation d'assurance)
 */
@Entity
@Table(name = "sinistres", indexes = {
    @Index(name = "idx_contrat_assurance", columnList = "contratAssuranceId"),
    @Index(name = "idx_created_by_user", columnList = "createdByUserId"),
    @Index(name = "idx_statut", columnList = "statut"),
    @Index(name = "idx_date_declaration", columnList = "dateDeclaration")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class Sinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeSinistre typeSinistre;

    @Column(nullable = false)
    private Instant dateDeclaration;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column
    private Long contratAssuranceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSinistre statut = StatutSinistre.DECLARE;

    @Column(length = 500)
    private String motifRefus;

    @Column(precision = 5, scale = 2)
    private BigDecimal tauxRemboursement;

    @Column(precision = 15, scale = 2)
    private BigDecimal estimationPerte;

    @Column(nullable = false)
    private Long createdByUserId;

    @OneToOne(mappedBy = "sinistre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Indemnisation indemnisation;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public Sinistre() {
        this.dateDeclaration = Instant.now();
    }

    /**
     * Vérifie si le sinistre peut être validé
     */
    public boolean canBeValidated() {
        return statut == StatutSinistre.EN_EVALUATION;
    }

    /**
     * Vérifie si le sinistre peut être refusé
     */
    public boolean canBeRefused() {
        return statut == StatutSinistre.EN_EVALUATION;
    }

    /**
     * Vérifie si le sinistre peut passer en évaluation
     */
    public boolean canStartEvaluation() {
        return statut == StatutSinistre.DECLARE;
    }

    /**
     * Vérifie si une indemnisation peut être créée pour ce sinistre
     */
    public boolean canCreateIndemnisation() {
        return statut == StatutSinistre.VALIDE && indemnisation == null;
    }
}