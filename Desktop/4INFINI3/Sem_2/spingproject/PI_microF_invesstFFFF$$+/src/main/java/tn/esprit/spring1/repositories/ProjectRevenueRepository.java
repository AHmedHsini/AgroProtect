package tn.esprit.spring1.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring1.entities.ProjectRevenue;
import java.util.List;
import java.util.Date;

@Repository
public interface ProjectRevenueRepository extends CrudRepository<ProjectRevenue, Long> {

    List<ProjectRevenue> findByProjectIdProject(Long idProject);

    List<ProjectRevenue> findByRevenueDateBetween(Date start, Date end);

    List<ProjectRevenue> findByRevenueAmountGreaterThan(Double amount);

    @Query("SELECT SUM(r.revenueAmount) FROM ProjectRevenue r WHERE r.project.idProject = :id")
    Double getTotalRevenueByProject(@Param("id") Long id);

    @Query("SELECT SUM(r.expenses) FROM ProjectRevenue r WHERE r.project.idProject = :id")
    Double getTotalExpensesByProject(@Param("id") Long id);
}

