package AgroProtect.repositories;

import AgroProtect.entities.RepaymentSchedule;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RepaymentScheduleRepository extends CrudRepository<RepaymentSchedule, Long> {
    Optional<RepaymentSchedule> findByCredit_IdAgriculturalCredit(Long id);

}