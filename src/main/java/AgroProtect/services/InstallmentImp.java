package AgroProtect.services;

import AgroProtect.entities.*;
import AgroProtect.repositories.AgriculturalCreditRepository;
import AgroProtect.repositories.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentImp implements IInstallmentService {

    private final InstallmentRepository installmentRepository;
    private final AgriculturalCreditRepository creditRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // AUTOMATIC: Runs every day at midnight to update penalties
    @Scheduled(cron = "0 0 0 * * ?") // At 00:00:00 every day
    @Transactional
    public void updateAllPenaltiesAutomatically() {
        log.info("Running automatic penalty update at {}", LocalDate.now());

        // Get all non-paid installments
        List<Installment> installments = installmentRepository
                .findByStatusIn(List.of(InstallmentStatus.PENDING, InstallmentStatus.PARTIALLY_PAID, InstallmentStatus.LATE));

        int updatedCount = 0;

        for (Installment ins : installments) {
            double oldTotal = ins.getTotalAmount();
            InstallmentStatus oldStatus = ins.getStatus();

            // Force recalculation
            entityManager.refresh(ins);
            ins.onLoad(); // This recalculates total with penalty

            // Save if changed
            if (ins.getTotalAmount() != oldTotal || ins.getStatus() != oldStatus) {
                installmentRepository.save(ins);
                updatedCount++;
                log.info("Auto-updated installment {}: total {} -> {} ({} days delay)",
                        ins.getId(), oldTotal, ins.getTotalAmount(), ins.getDelayDays());
            }
        }

        log.info("Automatic penalty update completed. Updated {} installments", updatedCount);
    }

    // Rest of your code...
    // (keep all existing methods: payInstallment, getById, getBySchedule, etc.)

    @Override
    @Transactional
    public Installment payInstallment(Long id, double amount) {
        entityManager.clear();

        Installment ins = installmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        if (ins.getStatus() == InstallmentStatus.PAID) {
            throw new RuntimeException("Installment already fully paid");
        }

        if (amount <= 0) {
            throw new RuntimeException("Payment amount must be positive");
        }

        entityManager.refresh(ins);

        double amountDue = ins.getAmountDue();

        if (amount > amountDue) {
            throw new RuntimeException(
                    String.format("Payment (%.2f) exceeds amount due (%.2f)", amount, amountDue));
        }

        ins.setPaidAmount(ins.getPaidAmount() + amount);

        if (ins.getPaidAmount() >= ins.getTotalAmount()) {
            ins.setPaymentDate(LocalDate.now());
            ins.setStatus(InstallmentStatus.PAID);
        } else {
            ins.setStatus(InstallmentStatus.PARTIALLY_PAID);
        }

        updateCreditPayment(ins, amount);

        Installment saved = installmentRepository.save(ins);

        log.info("Payment: {} TND for installment {}. Status: {}, Total: {}, Paid: {}, Remaining: {}",
                amount, id, saved.getStatus(), saved.getTotalAmount(),
                saved.getPaidAmount(), saved.getAmountDue());

        return saved;
    }

    private void updateCreditPayment(Installment ins, double amount) {
        AgriculturalCredit credit = ins.getRepaymentSchedule().getCredit();
        credit.setPaidAmount(credit.getPaidAmount() + amount);

        if (credit.getPaidAmount() >= credit.getTotalRepaymentAmount()) {
            credit.setStatus(CreditStatus.COMPLETED);
            credit.setClosedAt(LocalDate.now());
            log.info("Credit {} completed", credit.getIdAgriculturalCredit());
        }

        creditRepository.save(credit);
    }

    @Override
    @Transactional
    public Installment getById(Long id) {
        entityManager.clear();

        Installment ins = installmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        entityManager.refresh(ins);

        double oldTotal = ins.getTotalAmount();
        InstallmentStatus oldStatus = ins.getStatus();

        ins.onLoad();

        if (ins.getTotalAmount() != oldTotal || ins.getStatus() != oldStatus) {
            ins = installmentRepository.save(ins);
            log.info("Updated installment {}: total {} -> {}, status {} -> {}",
                    id, oldTotal, ins.getTotalAmount(), oldStatus, ins.getStatus());
        }

        return ins;
    }

    @Override
    @Transactional
    public List<Installment> getBySchedule(Long scheduleId) {
        entityManager.clear();

        List<Installment> list = installmentRepository.findByRepaymentSchedule_Id(scheduleId);

        for (Installment ins : list) {
            entityManager.refresh(ins);
            double oldTotal = ins.getTotalAmount();
            ins.onLoad();
            if (ins.getTotalAmount() != oldTotal) {
                installmentRepository.save(ins);
            }
        }

        return list;
    }

    @Override
    @Transactional
    public List<Installment> getByCredit(Long creditId) {
        entityManager.clear();

        List<Installment> list = installmentRepository
                .findByRepaymentSchedule_Credit_IdAgriculturalCredit(creditId);

        for (Installment ins : list) {
            entityManager.refresh(ins);
            double oldTotal = ins.getTotalAmount();
            ins.onLoad();
            if (ins.getTotalAmount() != oldTotal) {
                installmentRepository.save(ins);
            }
        }

        return list;
    }

    @Override
    @Transactional
    public List<Installment> getByStatus(InstallmentStatus status) {
        entityManager.clear();

        List<Installment> list = installmentRepository.findByStatus(status);
        list.forEach(entityManager::refresh);

        return list;
    }

    public PenaltySummary getPenaltySummary(Long creditId) {
        List<Installment> installments = getByCredit(creditId);

        double totalPenalty = installments.stream()
                .mapToDouble(Installment::getPenaltyAmount)
                .sum();

        long lateCount = installments.stream()
                .filter(Installment::isLate)
                .count();

        int maxDelayDays = installments.stream()
                .mapToInt(Installment::getDelayDays)
                .max()
                .orElse(0);

        double totalDue = installments.stream()
                .mapToDouble(Installment::getAmountDue)
                .sum();

        return PenaltySummary.builder()
                .totalPenaltyAmount(totalPenalty)
                .lateInstallmentCount((int) lateCount)
                .maximumDelayDays(maxDelayDays)
                .totalAmountDue(totalDue)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class PenaltySummary {
        private double totalPenaltyAmount;
        private int lateInstallmentCount;
        private int maximumDelayDays;
        private double totalAmountDue;
    }

    // Runs every 10 seconds to check for changes
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    @Transactional
    public void pollForPenaltyUpdates() {
        entityManager.clear();

        // Get all non-paid installments
        List<Installment> installments = installmentRepository
                .findByStatusIn(List.of(InstallmentStatus.PENDING, InstallmentStatus.PARTIALLY_PAID, InstallmentStatus.LATE));

        for (Installment ins : installments) {
            // Force refresh from database to see any manual due_date changes
            entityManager.refresh(ins);

            double oldTotal = ins.getTotalAmount();
            InstallmentStatus oldStatus = ins.getStatus();

            // Recalculate
            ins.onLoad();

            // Save if changed
            if (ins.getTotalAmount() != oldTotal || ins.getStatus() != oldStatus) {
                installmentRepository.save(ins);
                log.info("Auto-detected change for installment {}: total {} -> {}, status {} -> {}",
                        ins.getId(), oldTotal, ins.getTotalAmount(), oldStatus, ins.getStatus());
            }
        }
    }
}