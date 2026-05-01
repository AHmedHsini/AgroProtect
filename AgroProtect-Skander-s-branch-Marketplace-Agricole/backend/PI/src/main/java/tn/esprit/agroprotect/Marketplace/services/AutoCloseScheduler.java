package  tn.esprit.agroprotect.Marketplace.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import  tn.esprit.agroprotect.Marketplace.entities.Annonce;
import  tn.esprit.agroprotect.Marketplace.entities.StatusAnnonce;
import  tn.esprit.agroprotect.Marketplace.repositories.AnnonceRepository;

import java.util.List;

@Component
public class AutoCloseScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AutoCloseScheduler.class);

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private AnnonceService annonceService;

    @Autowired
    private MatchService matchService;

    @Scheduled(fixedDelay = 3600000) // Run every hour (3600000 ms)
    @Transactional
    public void closeFullyFundedAnnouncements() {
        logger.info("Starting scheduled auto-close check for fully funded announcements");

        List<Annonce> activeAnnouncements = annonceRepository.findByStatus(StatusAnnonce.DISPONIBLE);

        int closedCount = 0;
        int errorCount = 0;

        for (Annonce annonce : activeAnnouncements) {
            try {
                annonceService.autoCloseIfFullyFunded(annonce.getId());
                closedCount++;
                logger.info("Auto-closed announcement: id={}, titre='{}'", annonce.getId(), annonce.getTitre());
            } catch (Exception e) {
                errorCount++;
                logger.error("Error processing announcement id={} during auto-close: {}", annonce.getId(), e.getMessage(), e);
            }
        }

        logger.info("Auto-close check completed. {} announcements closed, {} errors", closedCount, errorCount);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void expireOldMatchesDaily() {
        logger.info("Starting scheduled match expiration task");
        try {
            int expiredCount = matchService.expireOldMatches();
            logger.info("Match expiration completed: {} matches expired", expiredCount);
        } catch (Exception e) {
            logger.error("Error in match expiration task: {}", e.getMessage(), e);
        }
    }
}

