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
}
