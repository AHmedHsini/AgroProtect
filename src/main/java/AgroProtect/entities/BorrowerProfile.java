package AgroProtect.entities;

import AgroProtect.useradapter.User; // ← TEMPORARY
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double monthlyIncome;
    private double existingDebt;
    private int previousCompletedCredits;
    private int previousDefaultedCredits;

    // Agricultural specific
    private String primaryActivity;
    private int yearsOfExperience;
    private String landLocation;
    private double totalLandArea;

    // USER LINK
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // Helper methods for risk engine
    public double getDebtToIncomeRatio() {
        if (monthlyIncome == 0) return 1.0;
        return existingDebt / monthlyIncome;
    }

    public boolean hasGoodCreditHistory() {
        return previousCompletedCredits > 0 && previousDefaultedCredits == 0;
    }
}