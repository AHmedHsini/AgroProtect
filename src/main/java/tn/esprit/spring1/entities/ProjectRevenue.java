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
}
