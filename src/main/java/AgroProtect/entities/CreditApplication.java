package AgroProtect.entities;

import AgroProtect.useradapter.User; // ← TEMPORARY: Change to identity.entity.User later
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCreditApplication;

    private double requestedAmount;
    private int requestedDurationMonths;
    private String purpose;

    private LocalDate submissionDate;
    private LocalDate evaluationDate;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String reason;
    private Double riskScore;

    @JsonIgnore
    @OneToOne(mappedBy = "creditApplication")
    private AgriculturalCredit agriculturalCredit;

    // USER LINK - Uses adapter now, will use real entity later
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}