package tn.esprit.spring1.Services;

import tn.esprit.spring1.entities.Investor;

import java.util.List;

public interface IInvestorService {
    public Investor addInvestor(Investor Investor);

    public Investor updateInvestor(Investor Investor);

    public void deleteInvestor(Long idInvestor);

    public Investor getInvestorById(Long idInvestor);

    public List<Investor> getAllInvestor();

    public List<Investor> addAllInvestor(List<Investor> Investors);

    List<Investor> searchByName(String keyword);

    List<Investor> getAllInvestorsSortedByCapital();

}
