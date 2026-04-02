package tn.esprit.spring1.Services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.*;
import tn.esprit.spring1.repositories.InvestmentRepository;
import tn.esprit.spring1.repositories.InvestorRepository;
import tn.esprit.spring1.repositories.ProjectRepository;


import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class InvestmentServiceImpl implements IInvestmentService {

    InvestmentRepository chrep;
    InvestorRepository investorRep;
    ProjectRepository projectRep;

    @Override
    public Investment addInvestment(Investment investment) {

        Long idInvestor = investment.getInvestor().getIdInvestor();

        Investor investor = investorRep.findById(idInvestor)
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        if (investor.getAvailableCapital() < investment.getAmount()) {
            throw new RuntimeException("Capital insuffisant");
        }

        investor.setAvailableCapital(
                investor.getAvailableCapital() - investment.getAmount()
        );

        Long idProject = investment.getProject().getIdProject();

        Project project = projectRep.findById(idProject)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        double goal = project.getFundingGoal() != null && project.getFundingGoal() > 0
                ? project.getFundingGoal() : 1;

        double equity = (investment.getAmount() / goal) * 100;
        investment.setEquityShare(equity);

        double current = project.getCollectedAmount() != null ? project.getCollectedAmount() : 0;
        project.setCollectedAmount(current + investment.getAmount());

        investorRep.save(investor);
        projectRep.save(project);

        investment.setInvestor(investor);
        investment.setProject(project);

        return chrep.save(investment);
    }


    @Override
    public Investment updateInvestment(Investment Investment) {
        return chrep.save(Investment);
    }

    @Override
    public void deleteInvestment(Long idInvestment) {
        chrep.deleteById(idInvestment);
    }

    @Override
    public Investment getInvestmentById(Long idInvestment) {
        //return chrep.findById(idInvestment).get();
        return chrep.findById(idInvestment).orElse(null);
    }

    @Override
    public List<Investment> getAllInvestment() {
        return (List<Investment>) chrep.findAll();
        //return List.of((Investment) chrep.findAll());
    }

    @Override
    public List<Investment> addAllInvestment(List<Investment> Investments) {
        return (List<Investment>) chrep.saveAll(Investments);
        //return List.of((Investment) chrep.saveAll(Investments));
    }

    @Override
    public List<Investment> getInvestmentsByStatus(InvestmentStatus status) {
        return chrep.findByStatus(status);
    }

    @Override
    public List<Investment> getInvestmentsGreaterThan(double amount) {
        return chrep.findByAmountGreaterThan(amount);
    }

    @Override
    public List<Investment> getInvestmentsBetweenDates(Date start, Date end) {
        return chrep.findByInvestmentDateBetween(start, end);
    }

    @Override
    public List<Investment> getInvestmentsByInvestor(Long idInvestor) {
        return chrep.findByInvestorIdInvestor(idInvestor);
    }

    @Override
    public Double getTotalInvestedByInvestor(Long idInvestor) {
        return chrep.getTotalInvestedByInvestor(idInvestor);
    }

    @Override
    public boolean canInvestorInvest(Long idInvestor, double amount) {
        Investor investor = investorRep.findById(idInvestor).orElse(null);
        return investor != null && investor.getAvailableCapital() >= amount;
    }

    @Override
    public Double getAverageEquity() {
        return chrep.getAverageEquity();
    }

    @Override
    public Double getTotalInvested() {
        Double total = chrep.getTotalInvested();
        return total != null ? total : 0.0;
    }

}
