package tn.esprit.spring1.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring1.entities.Investor;
import org.springframework.data.domain.Sort;
import tn.esprit.spring1.entities.InvestorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



@Repository

public interface InvestorRepository extends JpaRepository<Investor, Long> {

    List<Investor> findByNameContaining(String name);

    List<Investor> findByType(InvestorType type);

    List<Investor> findByAvailableCapitalGreaterThan(double amount);



}
