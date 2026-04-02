package tn.esprit.spring1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.spring1.entities.Project;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByNameContaining(String keyword);

    List<Project> findBySector(String sector);

    List<Project> findByFundingGoalLessThan(Double amount);

    @Query("SELECT p FROM Project p WHERE p.collectedAmount >= p.fundingGoal")
    List<Project> getFullyFundedProjects();

    @Query("SELECT p FROM Project p WHERE (p.collectedAmount * 100.0 / p.fundingGoal) >= 80")
    List<Project> getProjectsAbove80Percent();
}
