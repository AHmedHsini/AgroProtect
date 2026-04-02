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

    // ================== GETTERS ==================
    public long getIdInvestment() {
        return idInvestment;
    }

    public double getAmount() {
        return amount;
    }

    public Date getInvestmentDate() {
        return investmentDate;
    }

    public double getEquityShare() {
        return equityShare;
    }

    public InvestmentStatus getStatus() {
        return status;
    }

    public Investor getInvestor() {
        return investor;
    }

    public Project getProject() {
        return project;
    }

    // ================== SETTERS ==================
    public void setIdInvestment(long idInvestment) {
        this.idInvestment = idInvestment;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setInvestmentDate(Date investmentDate) {
        this.investmentDate = investmentDate;
    }

    public void setEquityShare(double equityShare) {
        this.equityShare = equityShare;
    }

    public void setStatus(InvestmentStatus status) {
        this.status = status;
    }

    public void setInvestor(Investor investor) {
        this.investor = investor;
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
