package com.example.agroprotect.services;

import com.example.agroprotect.entities.Match;
import com.example.agroprotect.entities.StatusMatch;
import com.example.agroprotect.repositories.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Override
    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    @Override
    public Match updateMatch(Long id, Match match) {
        Match existingMatch = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        existingMatch.setMessage(match.getMessage());
        existingMatch.setMontantPropose(match.getMontantPropose());

        return matchRepository.save(existingMatch);
    }

    @Override
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

    @Override
    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    @Override
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @Override
    public List<Match> getMatchesByAnnonce(Long annonceId) {
        return matchRepository.findByAnnonceId(annonceId);
    }

    @Override
    public List<Match> getMatchesByInvestisseur(Long investisseurId) {
        return matchRepository.findByInvestisseurId(investisseurId);
    }

    @Override
    public List<Match> getMatchesByStatus(StatusMatch status) {
        return matchRepository.findByStatus(status);
    }

    @Override
    public Match updateStatus(Long id, StatusMatch status) {
        Match match = getMatchById(id);
        match.setStatus(status);
        return matchRepository.save(match);
    }
}