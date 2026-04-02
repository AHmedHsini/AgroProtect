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
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idPartner;

    private String name;

    @Enumerated(EnumType.STRING)
    private PartnerType partnerType;


    private String serviceProvided;

    private String region;

    private String contact;

    // ================== ASSOCIATION ==================

    // Partner (1) ---- (*) Partnership
    @OneToMany(mappedBy = "partner")
    @JsonIgnore
    private Set<Partnership> partnerships;

}
