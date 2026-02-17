package com.example.agroprotect.controllers;

import com.example.agroprotect.entities.Match;
import com.example.agroprotect.entities.StatusMatch;
import com.example.agroprotect.services.MatchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping({"/Match"})
public class MatchController {

    MatchService matchService;

    @PostMapping("/addMatch")
    public Match addMatch(@RequestBody Match match) {
        return matchService.createMatch(match);
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
    public Match updateMatch(@PathVariable Long id, @RequestBody Match match) {
        return matchService.updateMatch(id, match);
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
}