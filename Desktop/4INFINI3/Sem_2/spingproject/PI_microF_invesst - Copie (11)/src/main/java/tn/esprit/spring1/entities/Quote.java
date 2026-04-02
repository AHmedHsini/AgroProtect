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

    // ================== GETTERS ==================
    public Long getId() {
        return id;
    }

    public Double getProposedPrice() {
        return proposedPrice;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getIsBest() {
        return isBest;
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

    public Double getPricePerTon() {
        return pricePerTon;
    }

    public Integer getProcessingCapacity() {
        return processingCapacity;
    }

    public Boolean getTransportIncluded() {
        return transportIncluded;
    }

    public Partner getPartner() {
        return partner;
    }

    public Project getProject() {
        return project;
    }

    // ================== SETTERS ==================
    public void setId(Long id) {
        this.id = id;
    }

    public void setProposedPrice(Double proposedPrice) {
        this.proposedPrice = proposedPrice;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIsBest(Boolean isBest) {
        this.isBest = isBest;
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

    public void setPricePerTon(Double pricePerTon) {
        this.pricePerTon = pricePerTon;
    }

    public void setProcessingCapacity(Integer processingCapacity) {
        this.processingCapacity = processingCapacity;
    }

    public void setTransportIncluded(Boolean transportIncluded) {
        this.transportIncluded = transportIncluded;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}