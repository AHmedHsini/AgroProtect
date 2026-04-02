package tn.esprit.spring1.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.dto.QuoteRequest;
import tn.esprit.spring1.dto.QuoteResponse;
import tn.esprit.spring1.entities.*;
import tn.esprit.spring1.repositories.PartnerRepository;
import tn.esprit.spring1.repositories.ProjectRepository;
import tn.esprit.spring1.repositories.QuoteRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements IQuoteService {

    private final QuoteRepository quoteRepository;
    private final PartnerRepository partnerRepository;
    private final ProjectRepository projectRepository;
    private final PdfService pdfService;


    @Override
    public Quote createQuote(QuoteRequest request){

        Quote quote = new Quote();

        quote.setProposedPrice(request.getProposedPrice());
        quote.setDescription(request.getDescription());
        quote.setPartnerType(request.getPartnerType());

        // 🔥 Récupération du Partner
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        if (!partner.getPartnerType().equals(request.getPartnerType())) {
            throw new RuntimeException("Partner type mismatch with request");
        }

        quote.setPartner(partner);

        quote.setProject(
                projectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found"))
        );

        switch (request.getPartnerType()) {

            case EQUIPMENT_PROVIDER:
                quote.setEquipmentName(request.getEquipmentName());
                quote.setQuantity(request.getQuantity());
                quote.setDeliveryTimeDays(request.getDeliveryTimeDays());
                break;

            case INVESTOR:
                quote.setProposedInvestmentAmount(request.getProposedInvestmentAmount());
                quote.setExpectedReturnRate(request.getExpectedReturnRate());
                quote.setDurationMonths(request.getDurationMonths());
                break;

            case OLIVE_MILL:
                quote.setPricePerTon(request.getPricePerTon());
                quote.setProcessingCapacity(request.getProcessingCapacity());
                quote.setTransportIncluded(request.getTransportIncluded());
                break;

            default:
                throw new RuntimeException("Type non supporté");
        }

        Quote savedQuote = quoteRepository.save(quote);

        return savedQuote;
    }

    @Override
    public QuoteResponse selectBestQuote(Long projectId) {

        List<Quote> quotes = quoteRepository.findByProject_IdProject(projectId);

        if (quotes.isEmpty()) {
            throw new RuntimeException("Aucun devis trouvé");
        }

        Quote best = null;
        double bestScore = -1;

        for (Quote q : quotes) {

            double score = calculateScore(q);

            if (score > bestScore) {
                bestScore = score;
                best = q;
            }
        }

        // Reset
        quotes.forEach(q -> {
            q.setIsBest(false);
            quoteRepository.save(q);
        });

        // Set best
        if (best != null) {
            best.setIsBest(true);
            quoteRepository.save(best);
        }

        double success = 0.5;
        if (best != null && best.getProject() != null) {
            success = successProbability(best.getProject());
        }

        return new QuoteResponse(
                best,
                bestScore,
                explainChoice(best),
                success
        );
    }

    @Override
    public List<Quote> getQuotesByProject(Long projectId) {
        return quoteRepository.findByProject_IdProject(projectId);
    }


    private double simulateROI(Project project) {

        double total = 0;
        int simulations = 100;

        for (int i = 0; i < simulations; i++) {

            double randomFactor = 0.8 + (Math.random() * 0.4); // incertitude (80% → 120%)

            double simulatedReturn = project.getCollectedAmount() * randomFactor;

            total += simulatedReturn;
        }

        return total / simulations;
    }

    @Override
    public double successProbability(Project project) {

        double roi = simulateROI(project);

        if (roi > project.getFundingGoal()) return 0.9;
        if (roi > project.getFundingGoal() * 0.8) return 0.7;
        return 0.4;
    }

    @Override
    public String explainChoice(Quote q) {

        double success = 0.5;
        if (q.getProject() != null)
            success = successProbability(q.getProject());

        if (success > 0.8)
            return "High success probability";

        if (q.getExpectedReturnRate() != null && q.getExpectedReturnRate() > 15)
            return "High expected return";

        if (q.getDeliveryTimeDays() != null && q.getDeliveryTimeDays() < 5)
            return "Fast delivery";

        return "Best trade-off between cost, return and risk";
    }

    @Override
    public double calculateScore(Quote q) {

        double priceScore = 0;
        if (q.getProposedPrice() != null)
            priceScore = 1.0 / (q.getProposedPrice() + 1);

        double deliveryScore = 0;
        if (q.getDeliveryTimeDays() != null)
            deliveryScore = 1.0 / (q.getDeliveryTimeDays() + 1);

        double roiScore = 0;
        if (q.getExpectedReturnRate() != null)
            roiScore = q.getExpectedReturnRate() / 100;

        double projectSuccess = 0.5;
        if (q.getProject() != null)
            projectSuccess = successProbability(q.getProject());

        return (0.3 * priceScore) +
                (0.2 * deliveryScore) +
                (0.3 * roiScore) +
                (0.2 * projectSuccess);
    }

    @Override
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    @Override
    public void deleteQuote(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw new RuntimeException("Quote not found");
        }
        quoteRepository.deleteById(id);
    }

    @Override
    public Quote updateQuote(Long id, QuoteRequest request) {

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        quote.setProposedPrice(request.getProposedPrice());
        quote.setDescription(request.getDescription());

        switch (request.getPartnerType()) {

            case EQUIPMENT_PROVIDER:
                quote.setEquipmentName(request.getEquipmentName());
                quote.setQuantity(request.getQuantity());
                quote.setDeliveryTimeDays(request.getDeliveryTimeDays());
                break;

            case INVESTOR:
                quote.setProposedInvestmentAmount(request.getProposedInvestmentAmount());
                quote.setExpectedReturnRate(request.getExpectedReturnRate());
                quote.setDurationMonths(request.getDurationMonths());
                break;

            case OLIVE_MILL:
                quote.setPricePerTon(request.getPricePerTon());
                quote.setProcessingCapacity(request.getProcessingCapacity());
                quote.setTransportIncluded(request.getTransportIncluded());
                break;
        }

        return quoteRepository.save(quote);
    }

    @Override
    public List<QuoteResponse> selectBestQuotesForAllProjects() {

        List<Project> projects = projectRepository.findAll();
        List<QuoteResponse> results = new ArrayList<>();

        for (Project p : projects) {

            List<Quote> quotes = quoteRepository.findByProject_IdProject(p.getIdProject());

            if (quotes.isEmpty()) continue; // skip projets sans devis

            Quote best = null;
            double bestScore = -1;

            for (Quote q : quotes) {

                double score = calculateScore(q);

                if (score > bestScore) {
                    bestScore = score;
                    best = q;
                }
            }

            // reset
            for (Quote q : quotes) {
                q.setIsBest(false);
                quoteRepository.save(q);
            }

            if (best != null) {
                best.setIsBest(true);
                quoteRepository.save(best);

                double success = successProbability(p);
                String reason = explainChoice(best);

                results.add(new QuoteResponse(
                        best,
                        bestScore,
                        reason,
                        success
                ));
            }
        }

        return results;
    }
}