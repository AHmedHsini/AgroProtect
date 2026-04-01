package AgroProtect.services;

import AgroProtect.entities.*;
import AgroProtect.repositories.AgriculturalCreditRepository;
import AgroProtect.repositories.InstallmentRepository;
import AgroProtect.repositories.RepaymentScheduleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class RepaymentScheduleImp implements IRepaymentScheduleService {

    private AgriculturalCreditRepository creditRepository;
    private RepaymentScheduleRepository scheduleRepository;
    private InstallmentRepository installmentRepository;
    private EmailService emailService;  // ADD THIS

    @Override
    public RepaymentSchedule generateSchedule(Long creditId,
                                              PaymentFrequency frequency,
                                              AmortizationType amortizationType) {

        AgriculturalCredit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new RuntimeException("Credit not found"));

        if (credit.getStatus() != CreditStatus.ACTIVE)
            throw new RuntimeException("Schedule can only be generated for ACTIVE credit");

        if (scheduleRepository.findByCredit_IdAgriculturalCredit(creditId).isPresent()) {
            throw new RuntimeException("Schedule already exists for this credit");
        }

        int months = credit.getDurationMonths();
        int installmentCount = months;

        double principal = credit.getApprovedAmount();
        double rate = credit.getInterestRate();

        RepaymentSchedule schedule = RepaymentSchedule.builder()
                .startDate(credit.getDisbursementDate())
                .endDate(credit.getMaturityDate())
                .installmentCount(installmentCount)
                .paymentFrequency(frequency)
                .amortizationType(amortizationType)
                .totalPrincipal(principal)
                .totalInterest(principal * rate)
                .credit(credit)
                .build();

        schedule = scheduleRepository.save(schedule);

        List<Installment> installments = generateInstallments(schedule, credit, amortizationType);

        installmentRepository.saveAll(installments);

        schedule.setInstallments(installments);

        RepaymentSchedule saved = scheduleRepository.save(schedule);

        // SEND SCHEDULE EMAIL TO FARMER
        sendScheduleEmail(credit, saved, installments);

        return saved;
    }

    private void sendScheduleEmail(AgriculturalCredit credit,
                                   RepaymentSchedule schedule,
                                   List<Installment> installments) {
        try {
            String email = credit.getUser().getEmail();
            String farmerName = credit.getUser().getFullName();

            if (email != null && !email.isEmpty()) {
                emailService.sendScheduleCreatedEmail(email, farmerName, credit, schedule, installments);
                log.info("Schedule email sent to farmer: {}", email);
            } else {
                log.warn("No email found for credit user: {}", credit.getUser().getId());
            }
        } catch (Exception e) {
            log.error("Failed to send schedule email: {}", e.getMessage());
        }
    }

    private List<Installment> generateInstallments(RepaymentSchedule schedule,
                                                   AgriculturalCredit credit,
                                                   AmortizationType type) {

        List<Installment> list = new ArrayList<>();

        int n = credit.getDurationMonths();
        double principal = credit.getApprovedAmount();
        double rate = credit.getInterestRate();

        LocalDate dueDate = credit.getDisbursementDate();

        if (type == AmortizationType.LINEAR) {

            double principalPart = principal / n;

            for (int i = 1; i <= n; i++) {

                double remainingPrincipal = principal - (principalPart * (i - 1));
                double interestPart = remainingPrincipal * rate / n;

                double total = principalPart + interestPart;

                Installment ins = Installment.builder()
                        .installmentNumber(i)
                        .dueDate(dueDate.plusMonths(i))
                        .principalAmount(principalPart)
                        .interestAmount(interestPart)
                        .totalAmount(total)
                        .baseAmount(total)
                        .paidAmount(0)
                        .status(InstallmentStatus.PENDING)
                        .repaymentSchedule(schedule)
                        .build();

                list.add(ins);
            }

        } else { // ANNUITY

            double monthlyRate = rate / n;
            double annuity = (principal * monthlyRate) /
                    (1 - Math.pow(1 + monthlyRate, -n));

            double remaining = principal;

            for (int i = 1; i <= n; i++) {

                double interestPart = remaining * monthlyRate;
                double principalPart = annuity - interestPart;

                remaining -= principalPart;

                Installment ins = Installment.builder()
                        .installmentNumber(i)
                        .dueDate(dueDate.plusMonths(i))
                        .principalAmount(principalPart)
                        .interestAmount(interestPart)
                        .totalAmount(annuity)
                        .baseAmount(annuity)
                        .paidAmount(0)
                        .status(InstallmentStatus.PENDING)
                        .repaymentSchedule(schedule)
                        .build();

                list.add(ins);
            }
        }

        return list;
    }

    @Override
    public RepaymentSchedule getById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    @Override
    public RepaymentSchedule getByCreditId(Long creditId) {
        return scheduleRepository.findByCredit_IdAgriculturalCredit(creditId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    @Override
    public List<RepaymentSchedule> getAll() {
        return (List<RepaymentSchedule>) scheduleRepository.findAll();
    }
}