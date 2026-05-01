package  tn.esprit.agroprotect.Marketplace.services;

import tn.esprit.agroprotect.Marketplace.dtos.CreateMatchRequest;
import  tn.esprit.agroprotect.Marketplace.dtos.MatchResponseDTO;
import tn.esprit.agroprotect.Marketplace.dtos.UpdateMatchRequest;
import  tn.esprit.agroprotect.Marketplace.entities.Match;
import  tn.esprit.agroprotect.Marketplace.entities.StatusMatch;

import java.util.List;

public interface MatchService {

    Match createMatchFromRequest(CreateMatchRequest request);

    Match updateMatch(Long id, UpdateMatchRequest request);

    void deleteMatch(Long id);

    Match getMatchById(Long id);

    List<Match> getAllMatches();

    List<Match> getMatchesByAnnonce(Long annonceId);

    List<Match> getMatchesByInvestisseur(Long investisseurId);

    List<Match> getMatchesByStatus(StatusMatch status);

    Match updateStatus(Long id, StatusMatch status);

    MatchResponseDTO acceptMatch(Long id);

    int expireOldMatches();

    int expireMatchesOlderThanDays(int days);

    void sendMatchCreatedNotifications(Match match);

}