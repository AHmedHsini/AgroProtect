package tn.esprit.spring1.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring1.entities.Investment;
import java.util.List;
import java.util.Date;

import tn.esprit.spring1.entities.InvestmentStatus;

@Repository

public interface InvestmentRepository extends CrudRepository<Investment, Long> {

    List<Investment> findByStatus(InvestmentStatus status);

    List<Investment> findByAmountGreaterThan(double amount);

    List<Investment> findByInvestmentDateBetween(Date start, Date end);

    List<Investment> findByInvestorIdInvestor(Long idInvestor);

    @Query("SELECT SUM(i.amount) FROM Investment i WHERE i.investor.idInvestor = :id")
    Double getTotalInvestedByInvestor(@Param("id") Long id);

    @Query("SELECT AVG(i.equityShare) FROM Investment i")
    Double getAverageEquity();

    @Query("SELECT SUM(i.amount) FROM Investment i")
    Double getTotalInvested();

}
