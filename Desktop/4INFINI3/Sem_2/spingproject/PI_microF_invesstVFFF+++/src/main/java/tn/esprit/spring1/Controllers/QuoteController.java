package tn.esprit.spring1.Controllers;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.PdfService;
import tn.esprit.spring1.dto.QuoteRequest;
import tn.esprit.spring1.dto.QuoteResponse;
import tn.esprit.spring1.entities.Quote;
import tn.esprit.spring1.Services.IQuoteService;
import tn.esprit.spring1.repositories.QuoteRepository;


import java.util.List;

@RestController
@RequestMapping("/Quote")
@AllArgsConstructor
public class QuoteController {

    IQuoteService quoteService;
    private final PdfService pdfService;
    private final QuoteRepository quoteRepository;

    /*
     * Ajouter un devis
     */
    @PostMapping("/add")
    public Quote addQuote(@RequestBody QuoteRequest request) throws Exception {
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
    public QuoteResponse getBest(@PathVariable Long projectId) throws Exception {
        return quoteService.selectBestQuote(projectId);
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generatePdf(
            @PathVariable Long id,
            HttpServletRequest request) throws Exception {

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        double score = quoteService.calculateScore(quote);
        double success = quoteService.successProbability(quote.getProject());
        String reason = quoteService.explainChoice(quote);

        //  URL dynamique automatique ////////////////////////////******
        String baseUrl = "https://iluminada-precoccygeal-overcaustically.ngrok-free.dev";
        byte[] pdf = pdfService.generateQuotePdf(
                quote, score, success, reason, baseUrl
        );

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=quote_" + id + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/all")
    public List<Quote> getAllQuotes(){
        return quoteService.getAllQuotes();
    }

    @DeleteMapping("/delete/{id}")
    public String deleteQuote(@PathVariable Long id){
        quoteService.deleteQuote(id);
        return "Quote deleted successfully";
    }

    @PutMapping("/update/{id}")
    public Quote updateQuote(@PathVariable Long id,
                             @RequestBody QuoteRequest request){
        return quoteService.updateQuote(id, request);
    }

    @GetMapping("/best/all")
    public List<QuoteResponse> getBestForAllProjects() {
        return quoteService.selectBestQuotesForAllProjects();
    }


}