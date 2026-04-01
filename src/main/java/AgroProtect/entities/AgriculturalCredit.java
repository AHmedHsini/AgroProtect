package AgroProtect.entities;

import AgroProtect.useradapter.User; // ← TEMPORARY
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
public class AgriculturalCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgriculturalCredit;

    @Column(nullable = false, updatable = false)
    private double approvedAmount;

    @Column(nullable = false, updatable = false)
    private int durationMonths;

    @Column(nullable = false, updatable = false)
    private double interestRate;

    @Column(nullable = false, updatable = false)
    private double totalRepaymentAmount;

    @Column(nullable = false, updatable = false)
    private int gracePeriodMonths;

    @Column(nullable = false, updatable = false)
    private LocalDate disbursementDate;

    @Column(nullable = false, updatable = false)
    private LocalDate maturityDate;

    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;

    private LocalDate closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditStatus status;

    @Column(nullable = false)
    private double paidAmount;

    @JsonIgnore
    @OneToOne(optional = false)
    @JoinColumn(name = "credit_application_id", nullable = false, unique = true)
    private CreditApplication creditApplication;

    // USER LINK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}