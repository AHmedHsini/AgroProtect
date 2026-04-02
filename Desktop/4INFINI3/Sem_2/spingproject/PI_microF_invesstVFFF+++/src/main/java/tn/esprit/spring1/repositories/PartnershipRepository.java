package tn.esprit.spring1.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring1.entities.Partnership;
import tn.esprit.spring1.entities.PartnershipRole;

import java.util.Date;
import java.util.List;

@Repository

public interface PartnershipRepository extends CrudRepository<Partnership, Long> {

    List<Partnership> findByRole(PartnershipRole role);

    List<Partnership> findByStartDateBetween(Date start, Date end);

    List<Partnership> findByStartDateBeforeAndEndDateAfter(Date today1, Date today2);

    List<Partnership> findByEndDateBefore(Date today);

    @Query("SELECT AVG(DATEDIFF(p.endDate, p.startDate)) FROM Partnership p WHERE p.endDate IS NOT NULL")
    Double getAverageDuration();

    @Query("SELECT COUNT(p) FROM Partnership p WHERE p.partner.idPartner = :id")
    Long countByPartner(@Param("id") Long id);



}
