package tn.esprit.spring1.Services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.entities.Investor;
import tn.esprit.spring1.repositories.InvestorRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class InvestorServiceImpl implements IInvestorService {

    private InvestorRepository chrep;

    @Override
    public Investor addInvestor(Investor investor) {
        return chrep.save(investor);
    }

    @Override
    public Investor updateInvestor(Investor investor) {
        return chrep.save(investor);
    }

    @Override
    public void deleteInvestor(Long idInvestor) {
        chrep.deleteById(idInvestor);
    }

    @Override
    public Investor getInvestorById(Long idInvestor) {
        return chrep.findById(idInvestor).orElse(null);
    }

    @Override
    public List<Investor> getAllInvestor() {
        return chrep.findAll();
    }

    @Override
    public List<Investor> addAllInvestor(List<Investor> investors) {
        return chrep.saveAll(investors);
    }

    @Override
    public List<Investor> searchByName(String keyword) {
        return chrep.findByNameContaining(keyword);
    }

    @Override
    public List<Investor> getAllInvestorsSortedByCapital() {
        return chrep.findAll(Sort.by(Sort.Direction.DESC, "availableCapital"));
    }

}
