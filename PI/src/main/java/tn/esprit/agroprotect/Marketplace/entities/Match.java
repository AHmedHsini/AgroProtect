package com.example.agroprotect.entities;

import jakarta.persistence.*;
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
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    @Column(name = "investisseur_id", nullable = false)
    private Long investisseurId;

    @Column(name = "match_date")
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMatch status;

    @Column(length = 500)
    private String message;

    @Column(name = "montant_propose")
    private Double montantPropose;

    @PrePersist
    protected void onCreate() {
        matchDate = LocalDateTime.now();
        if (status == null) {
            status = StatusMatch.EN_ATTENTE;
        }
    }
}