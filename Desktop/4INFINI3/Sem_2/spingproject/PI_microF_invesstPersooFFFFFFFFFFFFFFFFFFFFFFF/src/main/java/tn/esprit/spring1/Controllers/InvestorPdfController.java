package tn.esprit.spring1.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.spring1.Services.IQuoteService;
import tn.esprit.spring1.dto.QuoteResponse;

@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class InvestorPdfController {

    private final IQuoteService quoteService;

    @GetMapping("/investor/{projectId}")
    public ResponseEntity<byte[]> getInvestorPdf(@PathVariable Long projectId) throws Exception {

        QuoteResponse response = quoteService.selectBestQuote(projectId);

        byte[] pdf = response.getInvestorPdf();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=investor-report.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
}
