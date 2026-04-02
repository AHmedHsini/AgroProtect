package tn.esprit.spring1.dto;

import lombok.Getter;
import lombok.Setter;
import tn.esprit.spring1.entities.PartnerType;

/*
 * DTO utilisé pour recevoir les données
 * depuis le Controller.
 */

@Getter
@Setter
public class QuoteRequest {

    private Double proposedPrice;
    private String description;
    private PartnerType partnerType;

    // Equipment
    private String equipmentName;
    private Integer quantity;
    private Integer deliveryTimeDays;

    // Investor
    private Double proposedInvestmentAmount;
    private Double expectedReturnRate;
    private Integer durationMonths;

    // Olive Mill
    private Double pricePerTon;
    private Integer processingCapacity;
    private Boolean transportIncluded;

    private Long partnerId;
    private Long projectId;
}