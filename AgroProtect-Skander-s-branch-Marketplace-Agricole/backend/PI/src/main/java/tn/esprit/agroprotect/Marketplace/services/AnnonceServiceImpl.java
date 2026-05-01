package tn.esprit.agroprotect.Marketplace.services;

import  tn.esprit.agroprotect.Marketplace.dtos.AnnonceSearchResponse;
import  tn.esprit.agroprotect.Marketplace.dtos.FundingProgressDTO;
import  tn.esprit.agroprotect.Marketplace.dtos.SearchAnnonceRequest;
import  tn.esprit.agroprotect.Marketplace.entities.*;
import  tn.esprit.agroprotect.Marketplace.exceptions.ResourceNotFoundException;
import  tn.esprit.agroprotect.Marketplace.repositories.AnnonceRepository;
import  tn.esprit.agroprotect.Marketplace.repositories.MatchRepository;
import  tn.esprit.agroprotect.Marketplace.repositories.NotificationHistoryRepository;
import  tn.esprit.agroprotect.Marketplace.repositories.NotificationTemplateRepository;
import  tn.esprit.agroprotect.Marketplace.services.EmailService;
import  tn.esprit.agroprotect.Marketplace.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnonceServiceImpl implements AnnonceService {

    private static final Logger logger = LoggerFactory.getLogger(AnnonceServiceImpl.class);

    @Autowired
    private AnnonceRepository annonceRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;
    @Autowired
    private NotificationHistoryRepository notificationHistoryRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;

    @Override
    public Annonce createAnnonce(Annonce annonce) {
        return annonceRepository.save(annonce);
    }

    @Override
    public Annonce updateAnnonce(Long id, Annonce annonce) {
        Annonce existingAnnonce = annonceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce not found with id: " + id));

        existingAnnonce.setTitre(annonce.getTitre());
        existingAnnonce.setDescription(annonce.getDescription());
        existingAnnonce.setTypeAnnonce(annonce.getTypeAnnonce());
        existingAnnonce.setTargetAmount(annonce.getTargetAmount());

        return annonceRepository.save(existingAnnonce);
    }

    @Override
    public void deleteAnnonce(Long id) {
        annonceRepository.deleteById(id);
    }

    @Override
    public Annonce getAnnonceById(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce not found with id: " + id));
    }

    @Override
    public List<Annonce> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    @Override
    public List<Annonce> getAnnoncesByCreateur(Long createurId) {
        return annonceRepository.findByCreateurId(createurId);
    }

    @Override
    public List<Annonce> getAnnoncesByStatus(StatusAnnonce status) {
        return annonceRepository.findByStatus(status);
    }

    @Override
    public List<Annonce> getAnnoncesByType(TypeAnnonce typeAnnonce) {
        return annonceRepository.findByTypeAnnonce(typeAnnonce);
    }

    @Override
    public Annonce updateStatus(Long id, StatusAnnonce status) {
        Annonce annonce = getAnnonceById(id);
        annonce.setStatus(status);
        return annonceRepository.save(annonce);
    }

    @Override
    public FundingProgressDTO getFundingProgress(Long annonceId) {
        Annonce annonce = getAnnonceById(annonceId);

        List<Match> allMatches = matchRepository.findByAnnonceId(annonceId);
        List<Match> acceptedMatches = matchRepository.findByAnnonceIdAndStatus(annonceId, StatusMatch.ACCEPTE);
        List<Match> pendingMatches = matchRepository.findByAnnonceIdAndStatus(annonceId, StatusMatch.EN_ATTENTE);

        Double fundedAmount = acceptedMatches.stream()
                .mapToDouble(Match::getMontantPropose)
                .sum();

        Double progressPercentage = annonce.getTargetAmount() > 0
                ? (fundedAmount / annonce.getTargetAmount()) * 100
                : 0.0;

        return new FundingProgressDTO(
                annonce.getId(),
                annonce.getTitre(),
                annonce.getTargetAmount(),
                fundedAmount,
                progressPercentage,
                allMatches.size(),
                acceptedMatches.size(),
                pendingMatches.size(),
                annonce.getStatus().toString()
        );
    }

    @Override
    @Transactional
    public void checkAndCloseAnnouncement(Long annonceId, Long investisseurId) {
        logger.info("Checking funding status for annonceId={}, investisseurId={}", annonceId, investisseurId);

        Annonce annonce;
        try {
            annonce = getAnnonceById(annonceId);
        } catch (ResourceNotFoundException e) {
            logger.error("Annonce not found: {}", annonceId);
            throw e;
        }

        if (annonce.getStatus() == StatusAnnonce.NON_DISPONIBLE) {
            logger.info("Annonce {} already closed, skipping", annonceId);
            return;
        }

        List<Match> acceptedMatches = matchRepository.findByAnnonceIdAndStatus(annonceId, StatusMatch.ACCEPTE);
        Double fundedAmount = acceptedMatches.stream()
                .mapToDouble(Match::getMontantPropose)
                .sum();

        Double progressPercentage = annonce.getTargetAmount() > 0
                ? (fundedAmount / annonce.getTargetAmount()) * 100
                : 0.0;

        logger.info("Annonce {}: funded={}, target={}, progress={:.2f}%",
                annonceId, fundedAmount, annonce.getTargetAmount(), progressPercentage);

        checkAndSendMilestoneNotifications(annonce, investisseurId, progressPercentage, fundedAmount);

        if (fundedAmount >= annonce.getTargetAmount()) {
            logger.info("Annonce {} fully funded ({} >= {}). Closing...",
                    annonceId, fundedAmount, annonce.getTargetAmount());

            if (annonce.getStatus() == StatusAnnonce.DISPONIBLE) {
                annonce.setStatus(StatusAnnonce.NON_DISPONIBLE);
                annonce = annonceRepository.save(annonce); // Save with version increment
                logger.info("Annonce {} closed successfully", annonceId);

                createFundingCompleteNotification(annonce);
            } else {
                logger.info("Annonce {} status changed to {} by another process, skipping",
                        annonceId, annonce.getStatus());
            }
        }
    }

    @Override
    @Transactional
    public void autoCloseIfFullyFunded(Long annonceId) {
        logger.info("Auto-close check for annonce {}", annonceId);

        Annonce annonce = getAnnonceById(annonceId);

        if (annonce.getStatus() == StatusAnnonce.NON_DISPONIBLE) {
            logger.debug("Annonce {} already closed, skipping", annonceId);
            return;
        }

        List<Match> acceptedMatches = matchRepository.findByAnnonceIdAndStatus(annonceId, StatusMatch.ACCEPTE);
        Double fundedAmount = acceptedMatches.stream()
                .mapToDouble(Match::getMontantPropose)
                .sum();

        if (fundedAmount >= annonce.getTargetAmount()) {
            logger.info("Annonce {} fully funded ({} >= {}). Auto-closing...",
                    annonceId, fundedAmount, annonce.getTargetAmount());

            if (annonce.getStatus() == StatusAnnonce.DISPONIBLE) {
                annonce.setStatus(StatusAnnonce.NON_DISPONIBLE);
                annonce = annonceRepository.save(annonce);
                logger.info("Annonce {} closed successfully", annonceId);

                createFundingCompleteNotification(annonce);
            } else {
                logger.info("Annonce {} status changed to {} by another process, skipping auto-close",
                        annonceId, annonce.getStatus());
            }
        } else {
            logger.debug("Annonce {} not fully funded: {} < {}",
                    annonceId, fundedAmount, annonce.getTargetAmount());
        }
    }

    private void createFundingCompleteNotification(Annonce annonce) {
        NotificationTemplate template = notificationTemplateRepository.findByCode("FUNDING_COMPLETE")
                .orElse(null);

        if (template != null) {
            NotificationHistory notification = new NotificationHistory();
            notification.setTo(annonce.getCreateurId());
            notification.setSubject(template.getSubject());
            String content = template.getBody().replace("{titre}", annonce.getTitre());
            notification.setContent(content);
            notification.setTemplateId(template.getId());

            // ✅ FIX: Wrap save in try-catch to handle recipient_id constraint during dev
            try {
                notificationHistoryRepository.save(notification);
            } catch (Exception e) {
                logger.warn("⚠️  Could not save notification history (recipient_id constraint) - email will still send: {}", e.getMessage());
            }

            try {
                String creatorEmail = userService.getEmailByUserId(annonce.getCreateurId());
                emailService.sendEmail(creatorEmail, template.getSubject(), content);
            } catch (Exception e) {
                System.err.println("Failed to send funding complete email to creator " + annonce.getCreateurId() + ": " + e.getMessage());
            }
        }
    }


    private void checkAndSendMilestoneNotifications(Annonce annonce, Long investisseurId, Double progressPercentage, Double fundedAmount) {
        System.out.println("[DEBUG] checkAndSendMilestoneNotifications called. progress=" + progressPercentage + "%, fundedAmount=" + fundedAmount);
        if (annonce.getLastMilestoneNotified() == null) {
            annonce.setLastMilestoneNotified(0);
        }

        int lastNotified = annonce.getLastMilestoneNotified();
        System.out.println("[DEBUG] lastNotified=" + lastNotified);
        int newMilestone = 0;

        if (progressPercentage >= 100 && lastNotified < 100) {
            newMilestone = 100;
        } else if (progressPercentage >= 75 && lastNotified < 75) {
            newMilestone = 75;
        } else if (progressPercentage >= 50 && lastNotified < 50) {
            newMilestone = 50;
        } else if (progressPercentage >= 25 && lastNotified < 25) {
            newMilestone = 25;
        }

        System.out.println("[DEBUG] newMilestone=" + newMilestone);
        if (newMilestone > 0) {
            sendMilestoneNotification(annonce, investisseurId, newMilestone, fundedAmount, "3");
            sendMilestoneNotification(annonce, annonce.getCreateurId(), newMilestone, fundedAmount, "4");
            annonce.setLastMilestoneNotified(newMilestone);
            annonceRepository.save(annonce);
            System.out.println("[DEBUG] Milestone notifications sent and lastMilestoneNotified updated to " + newMilestone);
        } else {
            System.out.println("[DEBUG] No new milestone crossed");
        }
    }


    private void sendMilestoneNotification(Annonce annonce, Long toUserId, int milestone, Double fundedAmount, String templateCode) {
        NotificationTemplate template = notificationTemplateRepository.findByCode(templateCode)
                .orElse(null);

        if (template != null) {
            NotificationHistory notification = new NotificationHistory();
            notification.setTo(toUserId);
            notification.setSubject(template.getSubject());
            String content = template.getBody()
                    .replace("{titre}", annonce.getTitre())
                    .replace("{milestone}", milestone + "%")
                    .replace("{fundedAmount}", fundedAmount != null ? fundedAmount.toString() : "0");
            notification.setContent(content);
            notification.setTemplateId(template.getId());
            notification.setStatus( tn.esprit.agroprotect.Marketplace.entities.StatusNotification.ENVOYE);

            // ✅ FIX: Wrap save in try-catch to handle recipient_id constraint during dev
            try {
                notificationHistoryRepository.save(notification);
            } catch (Exception e) {
                logger.warn("⚠️  Could not save notification history (recipient_id constraint) - email will still send: {}", e.getMessage());
            }

            try {
                String recipientEmail = userService.getEmailByUserId(toUserId);
                emailService.sendEmail(recipientEmail, template.getSubject(), content);
            } catch (Exception e) {
                System.err.println("Failed to send milestone email to user " + toUserId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public AnnonceSearchResponse searchAnnonces(SearchAnnonceRequest request) {
        Sort.Direction direction = request.isSortDesc() ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, request.getSortBy());
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Annonce> annoncePage = annonceRepository.searchAnnonces(
                request.getSearch() != null && !request.getSearch().isEmpty() ? request.getSearch() : null,
                request.getType(),
                request.getStatus(),
                request.getLocation() != null && !request.getLocation().isEmpty() ? request.getLocation() : null,
                request.getMinAmount(),
                request.getMaxAmount(),
                pageable
        );

        List<AnnonceSearchResponse.AnnonceItem> items = annoncePage.getContent().stream()
                .map(this::convertToAnnonceItem)
                .collect(Collectors.toList());

        AnnonceSearchResponse.PaginationInfo pagination = new AnnonceSearchResponse.PaginationInfo(
                request.getPage(),
                request.getSize(),
                annoncePage.getTotalElements(),
                annoncePage.getTotalPages(),
                annoncePage.isLast()
        );

        return new AnnonceSearchResponse(items, pagination);
    }

    private AnnonceSearchResponse.AnnonceItem convertToAnnonceItem(Annonce annonce) {
        try {
            FundingProgressDTO progress = getFundingProgress(annonce.getId());

            return new AnnonceSearchResponse.AnnonceItem(
                    annonce.getId(),
                    annonce.getTypeAnnonce(),
                    annonce.getTitre(),
                    annonce.getDescription(),
                    annonce.getStatus(),
                    annonce.getDatePublication(),
                    annonce.getCreateurId(),
                    annonce.getTargetAmount(),
                    annonce.getLastMilestoneNotified(),
                    annonce.getLocation(),
                    annonce.getTargetDurationMonths(),
                    annonce.getLastModified(),
                    progress.getFundedAmount(),
                    progress.getProgressPercentage(),
                    progress.getAcceptedInvestors() != null ? progress.getAcceptedInvestors() : 0
            );
        } catch (ResourceNotFoundException e) {
            logger.error("Error calculating funding progress for annonce {}: {}", annonce.getId(), e.getMessage());
            return new AnnonceSearchResponse.AnnonceItem(
                    annonce.getId(),
                    annonce.getTypeAnnonce(),
                    annonce.getTitre(),
                    annonce.getDescription(),
                    annonce.getStatus(),
                    annonce.getDatePublication(),
                    annonce.getCreateurId(),
                    annonce.getTargetAmount(),
                    annonce.getLastMilestoneNotified(),
                    annonce.getLocation(),
                    annonce.getTargetDurationMonths(),
                    annonce.getLastModified(),
                    0.0,
                    0.0,
                    0
            );
        }
    }
}