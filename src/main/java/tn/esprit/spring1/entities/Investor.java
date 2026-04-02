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

}
