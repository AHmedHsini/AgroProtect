package AgroProtect.repositories;

import AgroProtect.entities.Installment;
import AgroProtect.entities.InstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByRepaymentSchedule_Id(Long scheduleId);

    List<Installment> findByRepaymentSchedule_Credit_IdAgriculturalCredit(Long creditId);

    List<Installment> findByStatus(InstallmentStatus status);

    List<Installment> findByStatusIn(List<InstallmentStatus> statuses);

    // Find late installments (due date passed but not paid)
    @Query("SELECT i FROM Installment i WHERE i.dueDate < :today AND i.status != 'PAID'")
    List<Installment> findLateInstallments(@Param("today") LocalDate today);
}