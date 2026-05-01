package  tn.esprit.agroprotect.Marketplace.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tn.esprit.agroprotect.Marketplace.dtos.CreateMatchRequest;
import  tn.esprit.agroprotect.Marketplace.dtos.MatchResponseDTO;
import tn.esprit.agroprotect.Marketplace.dtos.UpdateMatchRequest;
import  tn.esprit.agroprotect.Marketplace.entities.*;
import  tn.esprit.agroprotect.Marketplace.exceptions.ResourceNotFoundException;
import  tn.esprit.agroprotect.Marketplace.repositories.AnnonceRepository;
import  tn.esprit.agroprotect.Marketplace.repositories.MatchRepository;
import  tn.esprit.agroprotect.Marketplace.repositories.NotificationHistoryRepository;
import  tn.esprit.agroprotect.Marketplace.repositories.NotificationTemplateRepository;
import  tn.esprit.agroprotect.Marketplace.services.EmailService;
import  tn.esprit.agroprotect.Marketplace.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@AllArgsConstructor
@NoArgsConstructor
@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private AnnonceService annonceService;
    @Autowired
    private NotificationHistoryRepository notificationHistoryRepository;
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;
    @Autowired
    private AnnonceRepository annonceRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;

    @Override
    public Match createMatchFromRequest(CreateMatchRequest request) {
        Annonce annonce = annonceRepository.findById(request.getAnnonceId())
                .orElseThrow(() -> new RuntimeException("Annonce not found: " + request.getAnnonceId()));
        Match match = new Match();
        match.setAnnonce(annonce);  // ← Set the full entity here
        match.setInvestisseurId(request.getInvestisseurId());
        match.setMessage(request.getMessage());
        match.setMontantPropose(request.getMontantPropose());
        match.setStatus(request.getStatus() != null
                ? request.getStatus()
                : StatusMatch.EN_ATTENTE);
        return matchRepository.save(match);
    }

    public void sendMatchCreatedNotifications(Match match) {
        Long annonceId = match.getAnnonce().getId();
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new RuntimeException("Annonce not found: " + annonceId));

        Long investorId = match.getInvestisseurId();
        Long creatorId = annonce.getCreateurId();

        Optional<NotificationTemplate> investorTemplateOpt = notificationTemplateRepository.findByCode("1");
        if (investorTemplateOpt.isPresent()) {
            NotificationTemplate investorTemplate = investorTemplateOpt.get();
            String updatedContent = investorTemplate.getBody()
                    .replace("{titre}", annonce.getTitre())
                    .replace("{montant}", match.getMontantPropose().toString());

            NotificationHistory investorNotification = new NotificationHistory();
            investorNotification.setTo(investorId);
            investorNotification.setSubject(investorTemplate.getSubject());
            investorNotification.setContent(updatedContent);
            investorNotification.setTemplateId(investorTemplate.getId());
            investorNotification.setStatus( tn.esprit.agroprotect.Marketplace.entities.StatusNotification.ENVOYE);
            notificationHistoryRepository.save(investorNotification);

            try {
                String investorEmail = userService.getEmailByUserId(investorId);
                emailService.sendEmail(investorEmail, investorTemplate.getSubject(), updatedContent);
            } catch (Exception e) {
                System.err.println("Failed to send email to investor " + investorId + ": " + e.getMessage());
            }
        }

        if (creatorId != null && !creatorId.equals(investorId)) {
            Optional<NotificationTemplate> creatorTemplateOpt = notificationTemplateRepository.findByCode("2");
            if (creatorTemplateOpt.isPresent()) {
                NotificationTemplate creatorTemplate = creatorTemplateOpt.get();
                String updatedContent = creatorTemplate.getBody()
                        .replace("{titre}", annonce.getTitre())
                        .replace("{montant}", match.getMontantPropose().toString());

                NotificationHistory creatorNotification = new NotificationHistory();
                creatorNotification.setTo(creatorId);
                creatorNotification.setSubject(creatorTemplate.getSubject());
                creatorNotification.setContent(updatedContent);
                creatorNotification.setTemplateId(creatorTemplate.getId());
                creatorNotification.setStatus( tn.esprit.agroprotect.Marketplace.entities.StatusNotification.ENVOYE);
                notificationHistoryRepository.save(creatorNotification);

                // Send email
                try {
                    String creatorEmail = userService.getEmailByUserId(creatorId);
                    emailService.sendEmail(creatorEmail, creatorTemplate.getSubject(), updatedContent);
                } catch (Exception e) {
                    System.err.println("Failed to send email to creator " + creatorId + ": " + e.getMessage());
                }
            }
        }
    }

    public Match updateMatch(Long id, UpdateMatchRequest request) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // Fetch Annonce entity if changed
        if (request.getAnnonceId() != null) {
            Annonce annonce = annonceRepository.findById(request.getAnnonceId())
                    .orElseThrow(() -> new RuntimeException("Annonce not found"));
            match.setAnnonce(annonce);
        }


        match.setStatus(request.getStatus());
        match.setMessage(request.getMessage());
        match.setMontantPropose(request.getMontantPropose());

        return matchRepository.save(match);
    }

    @Override
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

    @Override
    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
    }

    @Override
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @Override
    public List<Match> getMatchesByAnnonce(Long annonceId) {
        return matchRepository.findByAnnonceId(annonceId);
    }

    @Override
    public List<Match> getMatchesByInvestisseur(Long investisseurId) {
        return matchRepository.findByInvestisseurId(investisseurId);
    }

    @Override
    public List<Match> getMatchesByStatus(StatusMatch status) {
        return matchRepository.findByStatus(status);
    }

    @Override
    public Match updateStatus(Long id, StatusMatch status) {
        Match match = getMatchById(id);
        match.setStatus(status);
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public MatchResponseDTO acceptMatch(Long id) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                Match match = matchRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Match not found"));

                if (match.getStatus() == StatusMatch.ACCEPTE) {
                    throw new IllegalStateException("Match is already accepted");
                }
                if (match.getStatus() == StatusMatch.REFUSE) {
                    throw new IllegalStateException("Cannot accept a refused match");
                }
                if (match.getStatus() == StatusMatch.TERMINE) {
                    throw new IllegalStateException("Cannot accept a terminated match");
                }

                Annonce annonce = annonceRepository.findById(match.getAnnonce().getId())
                        .orElseThrow(() -> new RuntimeException("Annonce not found"));
                if (annonce.getStatus() == StatusAnnonce.NON_DISPONIBLE) {
                    throw new IllegalStateException("Cannot accept match: announcement is already closed");
                }

                match.setStatus(StatusMatch.ACCEPTE);
                Match savedMatch = matchRepository.save(match);

                annonceService.checkAndCloseAnnouncement(savedMatch.getAnnonce().getId(), savedMatch.getInvestisseurId());

                return new MatchResponseDTO(
                        savedMatch.getId(),
                        savedMatch.getAnnonce().getId(),
                        savedMatch.getAnnonce().getTitre(),
                        savedMatch.getInvestisseurId(),
                        savedMatch.getMatchDate(),
                        savedMatch.getStatus(),
                        savedMatch.getMessage(),
                        savedMatch.getMontantPropose()
                );
            } catch (OptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw new IllegalStateException("Failed to accept match due to concurrent modifications. Please try again.", e);
                }
                try {
                    Thread.sleep(50 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        throw new IllegalStateException("Failed to accept match after retries");
    }

    @Override
    public int expireOldMatches() {
        return expireMatchesOlderThanDays(7);
    }

    @Override
    public int expireMatchesOlderThanDays(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        List<Match> expiredMatches = matchRepository.findByStatusAndMatchDateBefore(
                StatusMatch.EN_ATTENTE,
                cutoffDate
        );

        for (Match expiredMatch : expiredMatches) {
            expiredMatch.setStatus(StatusMatch.REFUSE);
            matchRepository.save(expiredMatch);
            createExpirationNotification(expiredMatch);
        }

        return expiredMatches.size();
    }

    private void createExpirationNotification(Match match) {
        NotificationTemplate template = notificationTemplateRepository.findByCode("MATCH_EXPIRED")
                .orElse(null);

        if (template != null) {
            Long annonceId = match.getAnnonce().getId();
            Annonce annonce = annonceRepository.findById(annonceId)
                    .orElseThrow(() -> new RuntimeException("Annonce not found: " + annonceId));

            NotificationHistory notification = new NotificationHistory();
            notification.setTo(match.getInvestisseurId());
            notification.setSubject(template.getSubject());
            String content = template.getBody()
                    .replace("{annonceTitre}", annonce.getTitre())
                    .replace("{days}", "7");
            notification.setContent(content);
            notification.setTemplateId(template.getId());
            notification.setStatus( tn.esprit.agroprotect.Marketplace.entities.StatusNotification.ENVOYE);
            notificationHistoryRepository.save(notification);

            try {
                String investorEmail = userService.getEmailByUserId(match.getInvestisseurId());
                emailService.sendEmail(investorEmail, template.getSubject(), content);
            } catch (Exception e) {
                System.err.println("Failed to send match expiration email: " + e.getMessage());
            }
        }
    }
}
