package  tn.esprit.agroprotect.Marketplace.controllers;

import jakarta.validation.Valid;
import tn.esprit.agroprotect.Marketplace.dtos.CreateMatchRequest;
import  tn.esprit.agroprotect.Marketplace.dtos.MatchResponseDTO;
import tn.esprit.agroprotect.Marketplace.dtos.UpdateMatchRequest;
import  tn.esprit.agroprotect.Marketplace.entities.Match;
import  tn.esprit.agroprotect.Marketplace.entities.StatusMatch;
import  tn.esprit.agroprotect.Marketplace.services.MatchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping({"/Match"})
public class MatchController {

    MatchService matchService;

    @PostMapping("/addMatch")
    public Match addMatch(@RequestBody @Valid CreateMatchRequest request) {
        return matchService.createMatchFromRequest(request);
    }

    @GetMapping("/getAll")
    public List<Match> getAllMatch() {
        return matchService.getAllMatches();
    }

    @GetMapping("/getById/{id}")
    public Match getById(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    @PutMapping("/updateMatch/{id}")
    public Match updateMatch(@PathVariable Long id, @RequestBody @Valid UpdateMatchRequest request) {
        return matchService.updateMatch(id, request);
    }

    @DeleteMapping("/deleteMatch/{id}")
    public void deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
    }

    @GetMapping("/getByAnnonce/{annonceId}")
    public List<Match> getByAnnonce(@PathVariable Long annonceId) {
        return matchService.getMatchesByAnnonce(annonceId);
    }

    @GetMapping("/getByInvestisseur/{investisseurId}")
    public List<Match> getByInvestisseur(@PathVariable Long investisseurId) {
        return matchService.getMatchesByInvestisseur(investisseurId);
    }

    @GetMapping("/getByStatus/{status}")
    public List<Match> getByStatus(@PathVariable StatusMatch status) {
        return matchService.getMatchesByStatus(status);
    }

    @PutMapping("/updateStatus/{id}/{status}")
    public Match updateStatus(@PathVariable Long id, @PathVariable StatusMatch status) {
        return matchService.updateStatus(id, status);
    }
    @PutMapping("/acceptMatch/{id}")
    public MatchResponseDTO acceptMatch(@PathVariable Long id) {
        return matchService.acceptMatch(id);
    }
    @PutMapping("/expireOldMatches")
    public String expireOldMatches() {
        int count = matchService.expireMatchesOlderThanDays(7);
        return count + " matches expired";
    }

    @PutMapping("/expireMatchesOlderThan/{days}")
    public void expireMatchesOlderThan(@PathVariable("days") int days) {
        matchService.expireMatchesOlderThanDays(days);
    }
}