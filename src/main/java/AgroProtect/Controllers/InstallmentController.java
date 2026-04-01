package AgroProtect.Controllers;

import AgroProtect.DTOs.PaymentResultDTO;
import AgroProtect.entities.Installment;
import AgroProtect.entities.InstallmentStatus;
import AgroProtect.services.IInstallmentService;
import AgroProtect.services.InstallmentImp;
import AgroProtect.services.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/installments")
@RequiredArgsConstructor
@Tag(name = "Installment & Payment", description = "Installment management with integrated Stripe payment")
public class InstallmentController {

    private final IInstallmentService installmentService;
    private final InstallmentImp installmentImp;
    private final PaymentService paymentService;

    // ===== INTERNAL PAYMENT (No Stripe) =====
    @Operation(summary = "Pay installment internally (no Stripe)")
    @PutMapping("/{id}/pay-internal")
    public Installment payInternal(@PathVariable Long id, @RequestParam double amount) {
        return installmentService.payInstallment(id, amount);
    }

    // ===== PERFECT STRIPE PAYMENT (One API) =====
    @Operation(
            summary = "PAY INSTALLMENT - Complete Stripe Payment",
            description = "Single API that: " +
                    "1. Calculates penalty if late, " +
                    "2. Creates Stripe PaymentIntent, " +
                    "3. Processes payment with test card, " +
                    "4. Updates installment to PAID, " +
                    "5. Sends email receipt, " +
                    "6. Returns full result."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment successful",
                    content = @Content(schema = @Schema(implementation = PaymentResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "Payment failed"),
            @ApiResponse(responseCode = "404", description = "Installment not found")
    })
    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentResultDTO> payWithStripe(
            @Parameter(description = "Installment ID", example = "14", required = true)
            @PathVariable Long id,

            @Parameter(description = "Currency (usd, eur, tnd)", example = "usd")
            @RequestParam(defaultValue = "usd") String currency,

            @Parameter(description = "Customer email for receipt", example = "farmer@gmail.com")
            @RequestParam(defaultValue = "farmer@agroprotect.com") String email,

            @Parameter(description = "Test card type (visa, mastercard, amex, decline)", example = "visa")
            @RequestParam(defaultValue = "visa") String cardType) throws StripeException {

        log.info("💳 PAYMENT REQUEST: installment={}, currency={}, email={}, card={}",
                id, currency, email, cardType);

        PaymentResultDTO result = paymentService.payInstallmentWithStripe(id, currency, email, cardType);

        // FIX: Use direct field access instead of getters
        log.info("✅ PAYMENT COMPLETE: {} {} - {}",
                result.totalAmount, result.currency, result.status);

        return ResponseEntity.ok(result);
    }

    // ===== GETTERS =====
    @GetMapping("/{id}")
    public Installment getById(@PathVariable Long id) {
        return installmentService.getById(id);
    }

    @GetMapping("/schedule/{scheduleId}")
    public List<Installment> getBySchedule(@PathVariable Long scheduleId) {
        return installmentService.getBySchedule(scheduleId);
    }

    @GetMapping("/credit/{creditId}")
    public List<Installment> getByCredit(@PathVariable Long creditId) {
        return installmentService.getByCredit(creditId);
    }

    @GetMapping("/status")
    public List<Installment> getByStatus(@RequestParam InstallmentStatus status) {
        return installmentService.getByStatus(status);
    }

    @GetMapping("/credit/{creditId}/penalty-summary")
    public InstallmentImp.PenaltySummary getPenaltySummary(@PathVariable Long creditId) {
        return installmentImp.getPenaltySummary(creditId);
    }
}