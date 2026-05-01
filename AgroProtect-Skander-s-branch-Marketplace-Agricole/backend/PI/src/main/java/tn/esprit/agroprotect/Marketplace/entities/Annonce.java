package  tn.esprit.agroprotect.Marketplace.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "annonces", indexes = {
    @Index(name = "idx_annonce_status", columnList = "status")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAnnonce typeAnnonce;

    @Column(nullable = false, length = 200)
    @Size(max = 200)
    @NotBlank
    private String titre;

    @Column(length = 2000)
    @Size(max = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAnnonce status;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "date_publication", nullable = false, updatable = false)
    private LocalDateTime datePublication;

    @Column(name = "createur_id", nullable = false)
    private Long createurId;


    @Column(nullable = false)
    @PositiveOrZero
    private Double targetAmount;

    @Column(length = 100)
    private String location;

    @Column(name = "target_duration_months")
    private Integer targetDurationMonths;

    @Column(name = "last_milestone_notified")
    private Integer lastMilestoneNotified = 0;

    @Version
    @Column(name = "version")
    private int version = 0;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "last_modified")
    private LocalDateTime lastModified;


    @PrePersist
    protected void onCreate() {
        datePublication = LocalDateTime.now();
        lastModified = LocalDateTime.now();
        if (status == null) {
            status = StatusAnnonce.EN_ATTENTE;
        }
    }


    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
    }
}