package AgroProtect.services;

import AgroProtect.entities.Installment;
import AgroProtect.entities.InstallmentStatus;

import java.util.List;

public interface IInstallmentService {

    Installment payInstallment(Long id, double amount);

    Installment getById(Long id);

    List<Installment> getBySchedule(Long scheduleId);

    List<Installment> getByCredit(Long creditId);

    List<Installment> getByStatus(InstallmentStatus status);
}