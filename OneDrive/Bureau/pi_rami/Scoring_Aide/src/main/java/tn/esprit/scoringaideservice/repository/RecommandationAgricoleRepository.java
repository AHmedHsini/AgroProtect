package tn.esprit.scoringaideservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.scoringaideservice.entity.RecommandationAgricole;

import java.util.List;

public interface RecommandationAgricoleRepository extends JpaRepository<RecommandationAgricole, Long> {

    List<RecommandationAgricole> findByScoreAgricoleId(Long scoreId);

}
