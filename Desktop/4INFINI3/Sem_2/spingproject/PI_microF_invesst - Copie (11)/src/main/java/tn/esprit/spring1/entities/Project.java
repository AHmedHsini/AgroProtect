package tn.esprit.spring1.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProject;

    private String name;

    private String sector;

    private Double fundingGoal;

    private Double collectedAmount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<ProjectRevenue> revenues;

    @OneToMany(mappedBy = "project")
    private List<Investment> investments;

    // ================== GETTERS ==================
    public Long getIdProject() {
        return idProject;
    }

    public String getName() {
        return name;
    }

    public String getSector() {
        return sector;
    }

    public Double getFundingGoal() {
        return fundingGoal;
    }

    public Double getCollectedAmount() {
        return collectedAmount;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public List<ProjectRevenue> getRevenues() {
        return revenues;
    }

    public List<Investment> getInvestments() {
        return investments;
    }

    // ================== SETTERS ==================
    public void setIdProject(Long idProject) {
        this.idProject = idProject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public void setFundingGoal(Double fundingGoal) {
        this.fundingGoal = fundingGoal;
    }

    public void setCollectedAmount(Double collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setRevenues(List<ProjectRevenue> revenues) {
        this.revenues = revenues;
    }

    public void setInvestments(List<Investment> investments) {
        this.investments = investments;
    }
}
