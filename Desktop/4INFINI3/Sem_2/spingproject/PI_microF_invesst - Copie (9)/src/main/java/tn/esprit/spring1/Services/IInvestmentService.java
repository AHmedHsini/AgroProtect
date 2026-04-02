package tn.esprit.spring1.Services;

import tn.esprit.spring1.entities.Investment;
import tn.esprit.spring1.entities.InvestmentStatus;
import tn.esprit.spring1.entities.Quote;

import java.util.Date;
import java.util.List;

public interface IInvestmentService {
    public Investment addInvestment(Investment Investment);

    public Investment updateInvestment(Investment Investment);

    public void deleteInvestment(Long idInvestment);

    public Investment getInvestmentById(Long idInvestment);

    public List<Investment> getAllInvestment();

    public List<Investment> addAllInvestment(List<Investment> Investments);

    //FILTRES

    List<Investment> getInvestmentsByStatus(InvestmentStatus status);

    List<Investment> getInvestmentsGreaterThan(double amount);

    List<Investment> getInvestmentsBetweenDates(Date start, Date end);

    List<Investment> getInvestmentsByInvestor(Long idInvestor);

    //METIERS AVANCES

    Double getTotalInvestedByInvestor(Long idInvestor);

    Double getAverageEquity();

    Double getTotalInvested();

    boolean canInvestorInvest(Long idInvestor, double amount);

    }
