package  tn.esprit.agroprotect.Marketplace.repositories;

import  tn.esprit.agroprotect.Marketplace.entities.Match;
import  tn.esprit.agroprotect.Marketplace.entities.StatusMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByAnnonceId(Long annonceId);

    List<Match> findByInvestisseurId(Long investisseurId);

    List<Match> findByStatus(StatusMatch status);

    List<Match> findByAnnonceIdAndStatus(Long annonceId, StatusMatch status);

    List<Match> findByInvestisseurIdAndStatus(Long investisseurId, StatusMatch status);

    List<Match> findByStatusAndMatchDateBefore(StatusMatch status, LocalDateTime date);
}