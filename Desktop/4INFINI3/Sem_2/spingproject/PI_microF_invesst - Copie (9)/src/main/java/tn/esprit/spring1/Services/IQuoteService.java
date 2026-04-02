package tn.esprit.spring1.Services;

import tn.esprit.spring1.dto.QuoteRequest;
import tn.esprit.spring1.dto.QuoteResponse;
import tn.esprit.spring1.entities.Project;
import tn.esprit.spring1.entities.Quote;

import java.util.List;

public interface IQuoteService {

    Quote createQuote(QuoteRequest request);

    List<Quote> getQuotesByProject(Long projectId);

    QuoteResponse selectBestQuote(Long projectId);

    public double calculateScore(Quote q);

    public String explainChoice(Quote q);

    public double successProbability(Project project);

    List<Quote> getAllQuotes();

    Quote updateQuote(Long id, QuoteRequest request);

    void deleteQuote(Long id);

    List<QuoteResponse> selectBestQuotesForAllProjects();
}