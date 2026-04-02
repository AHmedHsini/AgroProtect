package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.dto.QuoteRequest;
import tn.esprit.spring1.entities.Quote;
import tn.esprit.spring1.Services.IQuoteService;

import java.util.List;

@RestController
@RequestMapping("/Quote")
@AllArgsConstructor
public class QuoteController {

    IQuoteService quoteService;

    /*
     * Ajouter un devis
     */
    @PostMapping("/add")
    public Quote addQuote(@RequestBody QuoteRequest request){
        return quoteService.createQuote(request);
    }

    /*
     * Récupérer tous les devis d’un projet
     */
    @GetMapping("/byProject/{projectId}")
    public List<Quote> getByProject(@PathVariable Long projectId){
        return quoteService.getQuotesByProject(projectId);
    }

    /*
     * Sélectionner la meilleure offre
     */
    @GetMapping("/best/{projectId}")
    public Quote getBest(@PathVariable Long projectId){
        return quoteService.selectBestQuote(projectId);
    }
}