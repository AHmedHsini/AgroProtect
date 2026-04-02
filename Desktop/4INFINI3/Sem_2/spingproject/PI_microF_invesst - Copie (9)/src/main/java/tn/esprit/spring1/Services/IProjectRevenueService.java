package tn.esprit.spring1.Services;

import tn.esprit.spring1.entities.ProjectRevenue;

import java.util.Date;
import java.util.List;

public interface IProjectRevenueService {

    ProjectRevenue addRevenue(ProjectRevenue revenue);

    List<ProjectRevenue> getByProject(Long idProject);

    Double getTotalRevenue(Long idProject);

    Double getTotalExpenses(Long idProject);

    Double getProfit(Long idProject);

    Double getROI(Long idProject);

    List<ProjectRevenue> getBetweenDates(Date start, Date end);

    List<ProjectRevenue> getRevenueGreaterThan(Double amount);
}
