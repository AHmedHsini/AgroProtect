package com.example.agroprotect.repositories;

import com.example.agroprotect.entities.Match;
import com.example.agroprotect.entities.StatusMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByAnnonceId(Long annonceId);

    List<Match> findByInvestisseurId(Long investisseurId);

    List<Match> findByStatus(StatusMatch status);

    List<Match> findByAnnonceIdAndStatus(Long annonceId, StatusMatch status);

    List<Match> findByInvestisseurIdAndStatus(Long investisseurId, StatusMatch status);
}