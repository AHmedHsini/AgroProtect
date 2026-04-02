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
public class Partnership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idPartnership;

    @Enumerated(EnumType.STRING)
    private PartnershipRole role;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    // ================== ASSOCIATIONS ==================

    // Partnership (*) ---- (1) Project
    //@ManyToOne
   // private Project project;

    // Partnership (*) ---- (1) Partner
    @ManyToOne
    private Partner partner;

}
