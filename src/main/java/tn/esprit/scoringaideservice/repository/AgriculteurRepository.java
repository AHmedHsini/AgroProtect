package tn.esprit.scoringaideservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.scoringaideservice.entity.Agriculteur;

public interface AgriculteurRepository extends JpaRepository<Agriculteur, Long> {
}
