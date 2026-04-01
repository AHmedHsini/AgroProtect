package AgroProtect.Controllers;

import AgroProtect.DTOs.AIEnhancedRiskResultDTO;
import AgroProtect.DTOs.CreditApplicationCreateDTO;
import AgroProtect.entities.ApplicationStatus;
import AgroProtect.entities.BorrowerProfile;
import AgroProtect.entities.CreditApplication;
import AgroProtect.repositories.BorrowerProfileRepository;
import AgroProtect.services.CreditApplicationImp;
import AgroProtect.services.CreditRiskEngine;
import AgroProtect.services.ICreditApplicationService;
import AgroProtect.useradapter.User; // ← TEMPORARY
import AgroProtect.useradapter.UserRepository; // ← TEMPORARY
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/credit-applications")
@RequiredArgsConstructor
public class CreditApplicationController {

    private final ICreditApplicationService cser;
    private final CreditApplicationImp creditApplicationImp;
    private final UserRepository userRepository; // ← TEMPORARY
    private final BorrowerProfileRepository borrowerProfileRepository;
   private final CreditRiskEngine creditRiskEngine;

    @PostMapping
    public ResponseEntity<CreditApplication> create(@RequestBody CreditApplicationCreateDTO dto) {
        log.info("Creating credit application for user {}: {} TND",
                dto.getUserId(), dto.getRequestedAmount());

        // Get active user with roles
        User user = userRepository.findActiveByIdWithRoles(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Active user not found: " + dto.getUserId()));

        // Validate user can apply
        validateUserCanApply(user);

        // Get or validate borrower profile
        BorrowerProfile profile = borrowerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Borrower profile required. Complete profile first."));

        CreditApplication app = mapToEntity(dto, user);
        CreditApplication created = cser.addCreditApplication(app);

        return ResponseEntity.ok(created);
    }

    private void validateUserCanApply(User user) {
        // Check active
        if (!user.isActive()) {
            throw new RuntimeException("User account is not active. Status: " + user.getStatus());
        }

        // Check not deleted
        if (user.isDeleted()) {
            throw new RuntimeException("User account is deleted");
        }

        // Check role (optional - remove if not using roles yet)
        if (!user.hasRole("FARMER") && !user.hasRole("AGRICULTEUR") && !user.hasRole("USER")) {
            throw new RuntimeException("User must have FARMER role to apply for agricultural credit. Roles: " +
                    user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()));
        }
    }

    @GetMapping
    public List<CreditApplication> getAll() {
        return cser.getAllCreditApplication();
    }

    @GetMapping("/{id}")
    public CreditApplication getById(@PathVariable Long id) {
        return cser.getCreditApplicationById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cser.DeleteCreditApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/re-evaluate")
    public ResponseEntity<CreditApplication> reEvaluate(@PathVariable Long id) {
        CreditApplication reevaluated = creditApplicationImp.reEvaluate(id);
        return ResponseEntity.ok(reevaluated);
    }

    @GetMapping("/by-status/{status}")
    public List<CreditApplication> getByStatus(@PathVariable String status) {
        return cser.getAllCreditApplication().stream()
                .filter(app -> app.getStatus().name().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    @GetMapping("/triage")
    public List<CreditApplication> getTriageApplications() {
        return cser.getAllCreditApplication()
                .stream()
                .sorted(Comparator
                        .comparing(CreditApplication::getSubmissionDate) // oldest first
                        .thenComparing(CreditApplication::getRequestedAmount).reversed()

                )
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .collect(Collectors.toList());
    }

    private CreditApplication mapToEntity(CreditApplicationCreateDTO dto, User user) {
        return CreditApplication.builder()
                .requestedAmount(dto.getRequestedAmount())
                .requestedDurationMonths(dto.getRequestedDurationMonths())
                .purpose(dto.getPurpose())
                .user(user)
                .build();
    }
    @GetMapping("/{id}/ai-risk-report")
    public ResponseEntity<AIEnhancedRiskResultDTO> getAIRiskReport(@PathVariable Long id) {
        return ResponseEntity.ok(creditRiskEngine.getDetailedRiskReport(id));
    }
}