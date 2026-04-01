package AgroProtect.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MLModelTrainingScheduler {

    private final CreditRiskMLService mlService;

    /**
     * Retrain ML model every week on Sunday at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void retrainModelWeekly() {
        log.info("🔄 Starting weekly ML model retraining...");
        mlService.trainModel();
        log.info("✅ Weekly ML model retraining completed");
    }
}