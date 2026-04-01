package AgroProtect.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    private int installmentCount;

    @Enumerated(EnumType.STRING)
    private PaymentFrequency paymentFrequency;

    @Enumerated(EnumType.STRING)
    private AmortizationType amortizationType;

    private double totalPrincipal;
    private double totalInterest;
    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "credit_id", nullable = false, unique = true)
    private AgriculturalCredit credit;
    @JsonManagedReference
    @OneToMany(mappedBy = "repaymentSchedule", cascade = CascadeType.ALL)
    private List<Installment> installments;
}