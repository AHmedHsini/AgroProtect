package tn.esprit.spring1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring1.entities.Quote;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    // 🔥 IMPORTANT : relation ManyToOne project → id
    List<Quote> findByProject_IdProject(Long projectId);

}