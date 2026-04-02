package tn.esprit.spring1.Services;

import tn.esprit.spring1.dto.QuoteRequest;
import tn.esprit.spring1.entities.Quote;

import java.util.List;

public interface IQuoteService {

    Quote createQuote(QuoteRequest request);

    List<Quote> getQuotesByProject(Long projectId);

    Quote selectBestQuote(Long projectId);
}