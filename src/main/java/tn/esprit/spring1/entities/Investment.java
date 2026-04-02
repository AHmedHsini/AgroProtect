package tn.esprit.spring1.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idInvestment;

    private double amount;

    @Temporal(TemporalType.DATE)
    private Date investmentDate;

    private double equityShare;

    @Enumerated(EnumType.STRING)
    private InvestmentStatus status;

    // ================== ASSOCIATIONS ==================

    // Investment (*) ---- (1) Investor
    @ManyToOne
    private Investor investor;

    // Investment (*) ---- (1) Project
    @ManyToOne
    private Project project;

}
