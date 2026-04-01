package AgroProtect.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int installmentNumber;
    private LocalDate dueDate;
    private double principalAmount;
    private double interestAmount;

    // Base amount (original total without penalty) - stored in DB
    @Column(name = "base_amount")
    private Double baseAmount;

    // Current total (base + penalty when late) - stored in DB
    @Column(name = "total_amount")
    private double totalAmount;

    private double paidAmount;
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private InstallmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnore
    private RepaymentSchedule repaymentSchedule;

    // Flag to track if paid late (with penalty)
    @Column(name = "was_paid_late")
    private Boolean wasPaidLate = false;

    // ===== GETTERS FOR PAYMENT SYSTEM =====

    /**
     * Get base amount (original installment amount without penalty)
     */
    public double getBaseTotalAmount() {
        return baseAmount != null ? baseAmount : totalAmount;
    }

    /**
     * Get current total amount (includes penalty if late)
     */
    public double getTotalAmount() {
        // If already paid with penalty, return stored total
        if (Boolean.TRUE.equals(wasPaidLate)) {
            return totalAmount;
        }

        // If late and not paid, calculate with penalty
        if (getDelayDays() > 0 && status != InstallmentStatus.PAID) {
            return getBaseTotalAmount() + getPenaltyAmount();
        }

        return getBaseTotalAmount();
    }

    // ===== PENALTY CALCULATION =====

    public int getDelayDays() {
        if (status == InstallmentStatus.PAID && paymentDate != null) {
            if (paymentDate.isAfter(dueDate)) {
                return (int) ChronoUnit.DAYS.between(dueDate, paymentDate);
            }
            return 0;
        }

        LocalDate today = LocalDate.now();
        if (today.isAfter(dueDate)) {
            return (int) ChronoUnit.DAYS.between(dueDate, today);
        }
        return 0;
    }

    public double getPenaltyPercentage() {
        int days = getDelayDays();
        if (days <= 0) return 0.0;
        if (days <= 30) return 0.02;
        if (days <= 60) return 0.05;
        if (days <= 90) return 0.10;
        return 0.15;
    }

    public double getPenaltyAmount() {
        if (status == InstallmentStatus.PAID) {
            if (Boolean.TRUE.equals(wasPaidLate) && baseAmount != null) {
                return totalAmount - baseAmount;
            }
            return 0.0;
        }

        double remaining = getBaseTotalAmount() - paidAmount;
        if (remaining <= 0) return 0.0;

        return remaining * getPenaltyPercentage();
    }

    public double getAmountDue() {
        if (status == InstallmentStatus.PAID) return 0.0;
        return getTotalAmount() - paidAmount;
    }

    public boolean isLate() {
        return getDelayDays() > 0 && status != InstallmentStatus.PAID;
    }

    public InstallmentStatus getCurrentStatus() {
        if (paidAmount >= getTotalAmount() && paidAmount > 0) {
            return InstallmentStatus.PAID;
        }
        if (getDelayDays() > 0) {
            return InstallmentStatus.LATE;
        }
        if (paidAmount > 0) {
            return InstallmentStatus.PARTIALLY_PAID;
        }
        return InstallmentStatus.PENDING;
    }

    // ===== AUTO-UPDATE =====

    @PostLoad
    public void onLoad() {
        if (baseAmount == null) {
            baseAmount = totalAmount;
        }

        // Only recalculate if not paid yet
        if (status != InstallmentStatus.PAID) {
            this.status = getCurrentStatus();

            if (getDelayDays() > 0) {
                double penalty = getPenaltyAmount();
                this.totalAmount = baseAmount + penalty;
            }
        }
    }

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        if (baseAmount == null) {
            baseAmount = totalAmount;
        }

        // Check if becoming PAID
        boolean becomingPaid = (this.status != InstallmentStatus.PAID && getCurrentStatus() == InstallmentStatus.PAID);

        if (becomingPaid) {
            // If paid late (with penalty), preserve the total with penalty
            if (getDelayDays() > 0 || totalAmount > baseAmount) {
                wasPaidLate = true;
                // totalAmount stays as is (with penalty included)
            } else {
                wasPaidLate = false;
                totalAmount = baseAmount; // No penalty
            }

            this.paymentDate = LocalDate.now();
            this.status = InstallmentStatus.PAID;
        }
        // If already PAID - never change totalAmount again
        else if (this.status == InstallmentStatus.PAID) {
            // Keep everything as is
        }
        // Not paid yet - recalculate normally
        else {
            this.status = getCurrentStatus();

            if (getDelayDays() > 0) {
                double penalty = getPenaltyAmount();
                this.totalAmount = baseAmount + penalty;
            } else {
                this.totalAmount = baseAmount;
            }
        }
    }
}