package tn.esprit.scoringaideservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.scoringaideservice.entity.Culture;

public interface CultureRepository extends JpaRepository<Culture, Long> {
}
