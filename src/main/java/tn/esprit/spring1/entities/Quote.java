package tn.esprit.spring1.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
 * Entité unique représentant un devis.
 * Pas d’héritage.
 * Tous les champs sont regroupés ici.
 */

@Entity
@Getter
@Setter
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Prix général proposé
    private Double proposedPrice;

    private String description;

    private Boolean isBest = false;

    // Type du partenaire (important pour logique métier)
    @Enumerated(EnumType.STRING)
    private PartnerType partnerType;

    /*
     * -----------------------------
     * Champs spécifiques Equipment
     * -----------------------------
     */
    private String equipmentName;
    private Integer quantity;
    private Integer deliveryTimeDays;

    /*
     * -----------------------------
     * Champs spécifiques Investor
     * -----------------------------
     */
    private Double proposedInvestmentAmount;
    private Double expectedReturnRate;
    private Integer durationMonths;

    /*
     * -----------------------------
     * Champs spécifiques Olive Mill
     * -----------------------------
     */
    private Double pricePerTon;
    private Integer processingCapacity;
    private Boolean transportIncluded;

    /*
     * Relations
     */
    @ManyToOne
    private Partner partner;

    @ManyToOne
    private Project project;
}