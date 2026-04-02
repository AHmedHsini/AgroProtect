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

    // ================== GETTERS ==================
    public long getIdPartner() {
        return idPartner;
    }

    public String getName() {
        return name;
    }

    public PartnerType getPartnerType() {
        return partnerType;
    }

    public String getServiceProvided() {
        return serviceProvided;
    }

    public String getRegion() {
        return region;
    }

    public String getContact() {
        return contact;
    }

    public Set<Partnership> getPartnerships() {
        return partnerships;
    }

    // ================== SETTERS ==================
    public void setIdPartner(long idPartner) {
        this.idPartner = idPartner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPartnerType(PartnerType partnerType) {
        this.partnerType = partnerType;
    }

    public void setServiceProvided(String serviceProvided) {
        this.serviceProvided = serviceProvided;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setPartnerships(Set<Partnership> partnerships) {
        this.partnerships = partnerships;
    }

}
