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
@Table(name = "annonces")
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAnnonce typeAnnonce;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAnnonce status;

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    @Column(name = "createur_id", nullable = false)
    private Long createurId;

    @Column(name = "projet_id")
    private Long projetId;

    @Column(name = "reference_externe_id")
    private Long referenceExterneId;

    @PrePersist
    protected void onCreate() {
        datePublication = LocalDateTime.now();
        if (status == null) {
            status = StatusAnnonce.EN_ATTENTE;
        }
    }
}