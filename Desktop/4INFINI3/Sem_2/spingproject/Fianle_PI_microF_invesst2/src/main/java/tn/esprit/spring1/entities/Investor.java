package tn.esprit.spring1.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;


import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idInvestor;

    private String name;

    @Enumerated(EnumType.STRING)
    private InvestorType type;

    private double availableCapital;

    private String contact;

    // ================== ASSOCIATION ==================

    // Investor (1) ---- (*) Investment
    @OneToMany(mappedBy = "investor")
    @JsonIgnore
    private Set<Investment> investments;

    // ================== GETTERS ==================
    public long getIdInvestor() {
        return idInvestor;
    }

    public String getName() {
        return name;
    }

    public InvestorType getType() {
        return type;
    }

    public double getAvailableCapital() {
        return availableCapital;
    }

    public String getContact() {
        return contact;
    }

    public Set<Investment> getInvestments() {
        return investments;
    }

    // ================== SETTERS ==================
    public void setIdInvestor(long idInvestor) {
        this.idInvestor = idInvestor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(InvestorType type) {
        this.type = type;
    }

    public void setAvailableCapital(double availableCapital) {
        this.availableCapital = availableCapital;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setInvestments(Set<Investment> investments) {
        this.investments = investments;
    }

}
