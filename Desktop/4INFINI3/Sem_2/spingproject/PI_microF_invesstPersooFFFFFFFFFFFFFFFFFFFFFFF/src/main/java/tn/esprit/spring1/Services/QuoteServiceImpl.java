package tn.esprit.spring1.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.dto.QuoteRequest;
import tn.esprit.spring1.dto.QuoteResponse;
import tn.esprit.spring1.entities.*;
import tn.esprit.spring1.repositories.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements IQuoteService {

    private final QuoteRepository quoteRepository;
    private final PartnerRepository partnerRepository;
    private final ProjectRepository projectRepository;
    private final PdfService pdfService;
    private final InvestmentServiceImpl investmentService;
    private final InvestorRepository investorRepository;
    private final MLService mlService;
    private final InvestorPdfService investorPdfService;

    // ================= CREATE =================
    @Override
    public Quote createQuote(QuoteRequest request){

        Quote quote = new Quote();

        quote.setProposedPrice(request.getProposedPrice());
        quote.setDescription(request.getDescription());
        quote.setPartnerType(request.getPartnerType());

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

            case OLIVE_MILL:
                quote.setPricePerTon(request.getPricePerTon());
                quote.setProcessingCapacity(request.getProcessingCapacity());
                quote.setTransportIncluded(request.getTransportIncluded());
                break;

            default:
                throw new RuntimeException("Type non supporté");
        }

        return quoteRepository.save(quote);
    }

    // ================= SELECT BEST =================
    @Override
    public QuoteResponse selectBestQuote(Long projectId) throws Exception {
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

        // 🔄 Reset des best
        quotes.forEach(q -> {
            q.setIsBest(false);
            quoteRepository.save(q);
        });

        byte[] pdf = null;
        double success = 0.5;

        // ✅ Si meilleur devis trouvé
        if (best != null) {

            best.setIsBest(true);
            quoteRepository.save(best);

            // 🤖 ML réel
            success = mlService.predict(best.getProject());

            // 💰 Crowdfunding intelligent
            List<Investment> investments =
                    smartCrowdfunding(best.getProject(), best.getProposedPrice(), success);

            // 💾 Sauvegarde investissements
            for (Investment i : investments) {
                i.setStatus(InvestmentStatus.PENDING); // 🔥 validation admin plus tard
                investmentService.addInvestment(i);
            }

            // 🧠 Explication IA
            String explanation = explainInvestorChoice(success);

            // 📄 Génération PDF investor
            pdf = investorPdfService.generateInvestorReport(
                    best.getProject(),
                    investments,
                    success,
                    explanation,
                    "http://localhost:8083"
            );
        }

        // ✅ Création réponse
        QuoteResponse response = new QuoteResponse(
                best,
                bestScore,
                explainChoice(best),
                success
        );

        // 🔥 Injection PDF
        response.setInvestorPdf(pdf);

        return response;
    }

    // ================= BUSINESS =================
    private double simulateROI(Project project) {

        double total = 0;

        for (int i = 0; i < 100; i++) {
            double randomFactor = 0.8 + (Math.random() * 0.4);
            double simulatedReturn = project.getCollectedAmount() * randomFactor;
            total += simulatedReturn;
        }

        return total / 100;
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

        double success = successProbability(q.getProject());

        if (success > 0.8)
            return "High success probability";

        if (success > 0.7)
            return "High project profitability";

        if (q.getDeliveryTimeDays() != null && q.getDeliveryTimeDays() < 5)
            return "Fast delivery";

        return "Best trade-off between cost, return and risk";
    }

    @Override
    public double calculateScore(Quote q) {

        double priceScore = q.getProposedPrice() != null ?
                1.0 / (q.getProposedPrice() + 1) : 0;

        double deliveryScore = q.getDeliveryTimeDays() != null ?
                1.0 / (q.getDeliveryTimeDays() + 1) : 0;

        double roiScore = successProbability(q.getProject());

        double projectSuccess = successProbability(q.getProject());

        return (0.3 * priceScore) +
                (0.2 * deliveryScore) +
                (0.3 * roiScore) +
                (0.2 * projectSuccess);
    }

    // ================= AI =================
    private List<Investment> smartCrowdfunding(Project project, double amount, double success) {

        List<Investor> investors = investorRepository.findAll();
        List<Investment> result = new ArrayList<>();

        double remaining = amount;

        // 🔥 tri intelligent
        investors.sort((a, b) -> Double.compare(
                scoreInvestor(b, project, amount, success),
                scoreInvestor(a, project, amount, success)
        ));

        for (Investor inv : investors) {

            if (remaining <= 0) break;

            // 🔥 capital local (IMPORTANT)
            double available = inv.getAvailableCapital();

            if (available <= 0) continue;

            double maxPossible = available * (success > 0.7 ? 0.4 : 0.2);

            double contribution = Math.min(maxPossible, remaining);

            // 🔥 sécurité
            if (contribution <= 0 || contribution > available) continue;

            Investment i = new Investment();
            i.setInvestor(inv);
            i.setProject(project);
            i.setAmount(contribution);
            i.setInvestmentDate(new Date());
            i.setStatus(InvestmentStatus.PENDING);

            result.add(i);

            // 🔥 TRÈS IMPORTANT : simuler consommation du capital
            inv.setAvailableCapital(available - contribution);

            remaining -= contribution;
        }

        return result;
    }
    private double scoreInvestor(Investor inv, Project p, double amount, double success) {

        double capital = inv.getAvailableCapital() / amount;

        double risk = 1.0;

        if (inv.getType() == InvestorType.CONSERVATIVE && success < 0.5)
            risk = 1.2;

        if (inv.getType() == InvestorType.AGGRESSIVE && success > 0.7)
            risk = 1.2;

        double experience = inv.getInvestments() != null ?
                inv.getInvestments().size() : 0;

        return (0.4 * capital) +
                (0.4 * success) +
                (0.2 * risk) -
                (0.1 * experience);
    }

    private String explainInvestorChoice(double success) {

        if (success > 0.8)
            return "High profitability and low risk";

        if (success > 0.5)
            return "Balanced risk and return";

        return "High risk, distributed among multiple investors";
    }

    // ================= GET TOP 3 QUOTES =================
    @Override
    public List<QuoteResponse> getTop3Quotes(Long projectId) {

        List<Quote> quotes = quoteRepository.findByProject_IdProject(projectId);

        quotes.removeIf(q -> q.getPartner() == null);
        if (quotes.isEmpty()) {
            return new ArrayList<>();
        }

        quotes.sort((q1, q2) -> Double.compare(
                calculateScore(q2),
                calculateScore(q1)
        ));

        List<QuoteResponse> result = new ArrayList<>();
        int limit = Math.min(3, quotes.size());

        for (int i = 0; i < limit; i++) {
            Quote q = quotes.get(i);
            double score = calculateScore(q);
            QuoteResponse response = new QuoteResponse(
                    q,
                    score,
                    explainChoice(q),
                    successProbability(q.getProject())
            );
            result.add(response);
        }

        return result;
    }

    @Override
    public List<QuoteResponse> selectBestQuotesForAllProjects() {
        List<Project> projects = projectRepository.findAll();
        List<QuoteResponse> results = new ArrayList<>();

        for (Project p : projects) {
            try {
                QuoteResponse response = selectBestQuote(p.getIdProject());
                results.add(response);
            } catch (Exception e) {
                // Skip projects with no quotes or other errors
            }
        }

        return results;
    }

    @Override
    public List<Quote> getQuotesByProject(Long projectId) {
        return quoteRepository.findByProject_IdProject(projectId);
    }

    @Override
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    @Override
    public Quote updateQuote(Long id, QuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        if (request.getProposedPrice() != null) {
            quote.setProposedPrice(request.getProposedPrice());
        }
        if (request.getDescription() != null) {
            quote.setDescription(request.getDescription());
        }

        return quoteRepository.save(quote);
    }

    @Override
    public void deleteQuote(Long id) {
        quoteRepository.deleteById(id);
    }

    @Override
    public double mlPredictionScore(Quote q) {
        return mlService.predict(q.getProject());
    }

}