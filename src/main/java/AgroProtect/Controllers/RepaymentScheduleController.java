package AgroProtect.Controllers;
import AgroProtect.DTOs.CreditApplicationCreateDTO;
import AgroProtect.entities.*;
import AgroProtect.services.ICreditApplicationService;
import AgroProtect.services.IRepaymentScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedules")
@AllArgsConstructor
public class RepaymentScheduleController {

    private IRepaymentScheduleService service;

    @PostMapping("/generate/{creditId}")
    public RepaymentSchedule generate(@PathVariable Long creditId,
                                      @RequestParam PaymentFrequency frequency,
                                      @RequestParam AmortizationType type) {

        return service.generateSchedule(creditId, frequency, type);
    }

    @GetMapping
    public List<RepaymentSchedule> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RepaymentSchedule getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/credit/{creditId}")
    public RepaymentSchedule getByCredit(@PathVariable Long creditId) {
        return service.getByCreditId(creditId);
    }
    @GetMapping("/triage")
    public List<RepaymentSchedule> getTriageSchedules() {
        return service.getAll()
                .stream()
                .sorted(Comparator
                        .comparing(RepaymentSchedule::getEndDate) // soonest maturity first
                        .thenComparing(RepaymentSchedule::getTotalPrincipal).reversed()
                )
                .collect(Collectors.toList());
    }
}