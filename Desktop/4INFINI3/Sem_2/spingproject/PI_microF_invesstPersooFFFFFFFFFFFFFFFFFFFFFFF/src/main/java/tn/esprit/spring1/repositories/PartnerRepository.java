package tn.esprit.spring1.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring1.entities.Partner;
import tn.esprit.spring1.entities.PartnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findByRegion(String region);

    List<Partner> findByPartnerType(PartnerType type);

    @Query("SELECT p FROM Partner p ORDER BY SIZE(p.partnerships) DESC")
    List<Partner> findMostInvolvedPartners();

    @Query("SELECT COUNT(ps) FROM Partnership ps WHERE ps.partner.idPartner = :id AND ps.startDate <= CURRENT_DATE AND (ps.endDate IS NULL OR ps.endDate >= CURRENT_DATE)")
    Long countActivePartnerships(@Param("id") Long id);

}
