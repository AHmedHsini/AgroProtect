package AgroProtect.services;

import AgroProtect.entities.Installment;
import AgroProtect.entities.InstallmentStatus;
import AgroProtect.repositories.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReminderService {

    private final InstallmentRepository installmentRepository;
    private final EmailService emailService;

    /**
     * Send payment reminders every day at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyPaymentReminders() {
        log.info("Running daily payment reminder check at {}", LocalDate.now());

        // Get all pending installments due in next 7 days or overdue
        List<Installment> upcomingInstallments = installmentRepository
                .findByStatusIn(List.of(InstallmentStatus.PENDING, InstallmentStatus.LATE));

        for (Installment inst : upcomingInstallments) {
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), inst.getDueDate());

            // Remind if due in 3 days, 1 day, or overdue
            if (daysUntilDue == 3 || daysUntilDue == 1 || daysUntilDue <= 0) {
                sendReminder(inst, (int) daysUntilDue);
            }
        }
    }

    private void sendReminder(Installment installment, int daysUntilDue) {
        try {
            String email = installment.getRepaymentSchedule()
                    .getCredit().getUser().getEmail();
            String farmerName = installment.getRepaymentSchedule()
                    .getCredit().getUser().getFullName();

            if (email != null && !email.isEmpty()) {
                emailService.sendPaymentReminder(email, farmerName, installment,
                        installment.getAmountDue(), daysUntilDue);
                log.info("Reminder sent for installment {} to {}",
                        installment.getId(), email);
            }
        } catch (Exception e) {
            log.error("Failed to send reminder for installment {}: {}",
                    installment.getId(), e.getMessage());
        }
    }
}