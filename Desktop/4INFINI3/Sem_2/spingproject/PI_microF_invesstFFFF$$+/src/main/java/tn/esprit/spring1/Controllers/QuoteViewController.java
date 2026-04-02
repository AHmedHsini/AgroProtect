package tn.esprit.spring1.Controllers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring1.Services.IQuoteService;
import tn.esprit.spring1.dto.QuoteResponse;

import java.util.List;

@Controller
@RequestMapping("/view")
@AllArgsConstructor
public class QuoteViewController {

    private final IQuoteService quoteService;

    @GetMapping("/top3/{projectId}")
    public String viewTop3(Model model, @PathVariable String projectId) {

        Long id;

        try {
            id = Long.parseLong(projectId);
        } catch (Exception e) {
            id = 1L; // 🔥 pas de redirect !
        }

        List<QuoteResponse> quotes = quoteService.getTop3Quotes(id);
        model.addAttribute("quotes", quotes);

        return "top3";
    }
}