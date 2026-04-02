package tn.esprit.spring1.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring1.dto.QuoteRequest;
import tn.esprit.spring1.entities.*;
import tn.esprit.spring1.repositories.PartnerRepository;
import tn.esprit.spring1.repositories.ProjectRepository;
import tn.esprit.spring1.repositories.QuoteRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements IQuoteService {

    private final QuoteRepository quoteRepository;
    private final PartnerRepository partnerRepository;
    private final ProjectRepository projectRepository;

    @Override
    public Quote createQuote(QuoteRequest request) {

        Quote quote = new Quote();

        quote.setProposedPrice(request.getProposedPrice());
        quote.setDescription(request.getDescription());
        quote.setPartnerType(request.getPartnerType());

        // 🔥 Récupération du Partner
        Partner partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        // 🔥 Vérification cohérence du type
                if (!partner.getPartnerType().equals(request.getPartnerType())) {
                    throw new RuntimeException("Partner type mismatch with request");
                }

        // 🔥 On lie le partner au devis
        quote.setPartner(partner);

        quote.setProject(
                projectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new RuntimeException("Project not found"))
        );

        /*
         * LOGIQUE MÉTIER SELON PartnerType
         */
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

        return quoteRepository.save(quote);
    }

    @Override
    public Quote selectBestQuote(Long projectId) {

        List<Quote> quotes = quoteRepository.findByProject_IdProject(projectId);

        if (quotes.isEmpty()) {
            throw new RuntimeException("Aucun devis trouvé");
        }

        Quote best = quotes.stream()
                .filter(q -> q.getProposedPrice() != null)
                .min(Comparator.comparing(Quote::getProposedPrice))
                .orElse(null);

        // Reset tous à false
        quotes.forEach(q -> {
            q.setIsBest(false);
            quoteRepository.save(q);
        });

        // Mettre le meilleur à true
        if (best != null) {
            best.setIsBest(true);
            quoteRepository.save(best);
        }

        return best;
    }

    @Override
    public List<Quote> getQuotesByProject(Long projectId) {
        return quoteRepository.findByProject_IdProject(projectId);
    }
}