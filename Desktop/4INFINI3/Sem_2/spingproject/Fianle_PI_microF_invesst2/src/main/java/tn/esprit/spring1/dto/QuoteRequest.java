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

    // ================== GETTERS ==================
    public Double getProposedPrice() {
        return proposedPrice;
    }

    public String getDescription() {
        return description;
    }

    public PartnerType getPartnerType() {
        return partnerType;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getDeliveryTimeDays() {
        return deliveryTimeDays;
    }

    public Double getProposedInvestmentAmount() {
        return proposedInvestmentAmount;
    }

    public Double getExpectedReturnRate() {
        return expectedReturnRate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public Double getPricePerTon() {
        return pricePerTon;
    }

    public Integer getProcessingCapacity() {
        return processingCapacity;
    }

    public Boolean getTransportIncluded() {
        return transportIncluded;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getProjectId() {
        return projectId;
    }

    // ================== SETTERS ==================
    public void setProposedPrice(Double proposedPrice) {
        this.proposedPrice = proposedPrice;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPartnerType(PartnerType partnerType) {
        this.partnerType = partnerType;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setDeliveryTimeDays(Integer deliveryTimeDays) {
        this.deliveryTimeDays = deliveryTimeDays;
    }

    public void setProposedInvestmentAmount(Double proposedInvestmentAmount) {
        this.proposedInvestmentAmount = proposedInvestmentAmount;
    }

    public void setExpectedReturnRate(Double expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public void setPricePerTon(Double pricePerTon) {
        this.pricePerTon = pricePerTon;
    }

    public void setProcessingCapacity(Integer processingCapacity) {
        this.processingCapacity = processingCapacity;
    }

    public void setTransportIncluded(Boolean transportIncluded) {
        this.transportIncluded = transportIncluded;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}