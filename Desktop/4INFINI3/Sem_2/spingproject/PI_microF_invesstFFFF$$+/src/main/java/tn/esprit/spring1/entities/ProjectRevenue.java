package tn.esprit.spring1.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRevenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRevenue;

    private Double revenueAmount;

    private Date revenueDate;

    private Double expenses;

    @ManyToOne
    private Project project;

    // ================== GETTERS ==================
    public Long getIdRevenue() {
        return idRevenue;
    }

    public Double getRevenueAmount() {
        return revenueAmount;
    }

    public Date getRevenueDate() {
        return revenueDate;
    }

    public Double getExpenses() {
        return expenses;
    }

    public Project getProject() {
        return project;
    }

    // ================== SETTERS ==================
    public void setIdRevenue(Long idRevenue) {
        this.idRevenue = idRevenue;
    }

    public void setRevenueAmount(Double revenueAmount) {
        this.revenueAmount = revenueAmount;
    }

    public void setRevenueDate(Date revenueDate) {
        this.revenueDate = revenueDate;
    }

    public void setExpenses(Double expenses) {
        this.expenses = expenses;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
