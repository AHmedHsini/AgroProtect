package AgroProtect.services;

import AgroProtect.entities.*;

import java.util.List;

public interface IRepaymentScheduleService {

    RepaymentSchedule generateSchedule(Long creditId,
                                       PaymentFrequency frequency,
                                       AmortizationType amortizationType);
    RepaymentSchedule getById(Long id);

    RepaymentSchedule getByCreditId(Long creditId);

    List<RepaymentSchedule> getAll();

}