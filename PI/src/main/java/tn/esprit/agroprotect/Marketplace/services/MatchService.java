package com.example.agroprotect.services;

import com.example.agroprotect.entities.Match;
import com.example.agroprotect.entities.StatusMatch;

import java.util.List;

public interface MatchService {

    Match createMatch(Match match);

    Match updateMatch(Long id, Match match);

    void deleteMatch(Long id);

    Match getMatchById(Long id);

    List<Match> getAllMatches();

    List<Match> getMatchesByAnnonce(Long annonceId);

    List<Match> getMatchesByInvestisseur(Long investisseurId);

    List<Match> getMatchesByStatus(StatusMatch status);

    Match updateStatus(Long id, StatusMatch status);
}