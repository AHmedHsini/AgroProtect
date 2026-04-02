package tn.esprit.spring1.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.ProjectRevenue;
import tn.esprit.spring1.repositories.ProjectRevenueRepository;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class ProjectRevenueServiceImpl implements IProjectRevenueService {

    ProjectRevenueRepository chrep;

    @Override
    public ProjectRevenue addRevenue(ProjectRevenue revenue) {
        return chrep.save(revenue);
    }

    @Override
    public List<ProjectRevenue> getByProject(Long idProject) {
        return chrep.findByProjectIdProject(idProject);
    }

    @Override
    public Double getTotalRevenue(Long idProject) {
        return chrep.getTotalRevenueByProject(idProject);
    }

    @Override
    public Double getTotalExpenses(Long idProject) {
        return chrep.getTotalExpensesByProject(idProject);
    }

    @Override
    public Double getProfit(Long idProject) {
        Double revenue = getTotalRevenue(idProject);
        Double expenses = getTotalExpenses(idProject);
        return (revenue == null ? 0 : revenue) - (expenses == null ? 0 : expenses);
    }

    @Override
    public Double getROI(Long idProject) {
        Double revenue = getTotalRevenue(idProject);
        Double expenses = getTotalExpenses(idProject);

        if (revenue == null || revenue == 0) return 0.0;

        Double profit = getProfit(idProject);
        return (profit / revenue) * 100;
    }

    @Override
    public List<ProjectRevenue> getBetweenDates(Date start, Date end) {
        return chrep.findByRevenueDateBetween(start, end);
    }

    @Override
    public List<ProjectRevenue> getRevenueGreaterThan(Double amount) {
        return chrep.findByRevenueAmountGreaterThan(amount);
    }
}
