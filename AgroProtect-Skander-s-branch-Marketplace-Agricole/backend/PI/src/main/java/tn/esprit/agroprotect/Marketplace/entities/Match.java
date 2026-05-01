package  tn.esprit.agroprotect.Marketplace.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_match_status", columnList = "status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", nullable = true)
    private Annonce annonce;

    @Column(name = "investisseur_id", nullable = true)
    private Long investisseurId;

    @Column(name = "match_date")
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private StatusMatch status;

    @Column(length = 500)
    @Size(max = 500)
    private String message;

    @Column(name = "montant_propose")
    @PositiveOrZero
    private Double montantPropose;

    @PrePersist
    protected void onCreate() {
        matchDate = LocalDateTime.now();
        if (status == null) {
            status = StatusMatch.EN_ATTENTE;
        }
    }
}