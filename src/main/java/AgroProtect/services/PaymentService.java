package AgroProtect.services;

import AgroProtect.DTOs.PaymentResultDTO;
import AgroProtect.entities.Installment;
import AgroProtect.entities.PaymentTransaction;
import AgroProtect.repositories.PaymentTransactionRepository;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StripeService stripeService;
    private final IInstallmentService installmentService;
    private final PaymentTransactionRepository transactionRepository;
    private final EmailService emailService;
    private final Gson gson = new Gson();

    /**
     * PERFECT INTEGRATED PAYMENT - Everything in one method
     * Replaces both internal pay and Stripe processing
     */
    @Transactional
    public PaymentResultDTO payInstallmentWithStripe(Long installmentId, String currency,
                                                     String email, String cardType) throws StripeException {

        // ===== STEP 1: Get Installment with Auto-Calculated Penalty =====
        Installment installment = installmentService.getById(installmentId);
        double amountDue = installment.getAmountDue();

        if (amountDue <= 0) {
            throw new IllegalStateException(
                    String.format("Installment %d already paid. Status: %s",
                            installmentId, installment.getStatus()));
        }

        log.info("Step 1: Installment {} - Base: {}, Penalty: {}, Total: {}, Delay: {} days",
                installmentId,
                installment.getBaseTotalAmount(),
                installment.getPenaltyAmount(),
                amountDue,
                installment.getDelayDays());

        // ===== STEP 2: Create Stripe PaymentIntent =====
        String description = String.format("AgroProtect - Installment #%d (Credit #%d)",
                installment.getInstallmentNumber(),
                installment.getRepaymentSchedule().getCredit().getIdAgriculturalCredit());

        PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                amountDue, currency, installmentId, description, email);

        log.info("Step 2: PaymentIntent created - {}", paymentIntent.getId());

        // ===== STEP 3: Create PaymentMethod & Confirm =====
        String testToken = mapCardTypeToTestToken(cardType);
        PaymentMethod paymentMethod = createPaymentMethodFromToken(testToken);

        log.info("Step 3: PaymentMethod created - {} ({})",
                paymentMethod.getId(), paymentMethod.getCard().getBrand());

        PaymentIntent confirmedIntent = stripeService.confirmPaymentIntent(
                paymentIntent.getId(), paymentMethod.getId());

        if (!"succeeded".equals(confirmedIntent.getStatus())) {
            saveFailedTransaction(installmentId, confirmedIntent, confirmedIntent.getStatus());
            throw new IllegalStateException("Payment failed: " + confirmedIntent.getStatus());
        }

        log.info("Step 3: Payment confirmed - Status: {}", confirmedIntent.getStatus());

        // ===== STEP 4: Record Transaction =====
        PaymentTransaction transaction = saveSuccessfulTransaction(
                installment, confirmedIntent, paymentMethod, amountDue, currency);

        // ===== STEP 5: Update Installment (Internal Pay) =====
        Installment paidInstallment = installmentService.payInstallment(installmentId, amountDue);

        log.info("Step 5: Installment updated - Status: {}", paidInstallment.getStatus());

        // ===== STEP 6: Send Email Receipt =====
        PaymentResultDTO result = buildResult(installment, paidInstallment, transaction,
                confirmedIntent, paymentMethod, amountDue, currency);

        emailService.sendPaymentReceipt(email, result);
        log.info("Step 6: Receipt sent to: {}", email);

        return result;
    }

    private PaymentMethod createPaymentMethodFromToken(String tokenId) throws StripeException {
        Map<String, Object> card = new HashMap<>();
        card.put("token", tokenId);

        Map<String, Object> params = new HashMap<>();
        params.put("type", "card");
        params.put("card", card);

        return PaymentMethod.create(params);
    }

    private String mapCardTypeToTestToken(String cardType) {
        return switch (cardType.toLowerCase()) {
            case "visa" -> "tok_visa";
            case "mastercard" -> "tok_mastercard";
            case "amex" -> "tok_amex";
            case "discover" -> "tok_discover";
            case "decline" -> "tok_chargeDeclined";
            case "expired" -> "tok_chargeDeclinedExpiredCard";
            case "insufficient" -> "tok_chargeDeclinedInsufficientFunds";
            default -> "tok_visa";
        };
    }

    private PaymentTransaction saveSuccessfulTransaction(
            Installment installment, PaymentIntent paymentIntent,
            PaymentMethod paymentMethod, double amount, String currency) {

        PaymentTransaction transaction = PaymentTransaction.builder()
                .installmentId(installment.getId())
                .paymentIntentId(paymentIntent.getId())
                .paymentMethodId(paymentMethod.getId())
                .paymentMethodType(paymentMethod.getType())
                .cardLast4(paymentMethod.getCard().getLast4())
                .cardBrand(paymentMethod.getCard().getBrand())
                .amount(amount)
                .currency(currency.toUpperCase())
                .status("SUCCEEDED")
                .receiptUrl(getReceiptUrl(paymentIntent))
                .stripeResponse(gson.toJson(paymentIntent))
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    private void saveFailedTransaction(Long installmentId, PaymentIntent paymentIntent, String status) {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .installmentId(installmentId)
                .paymentIntentId(paymentIntent.getId())
                .amount((double) paymentIntent.getAmount() / 100)
                .currency(paymentIntent.getCurrency().toUpperCase())
                .status(status)
                .failureMessage("Payment failed: " + status)
                .stripeResponse(gson.toJson(paymentIntent))
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }

    private PaymentResultDTO buildResult(Installment installment, Installment paidInstallment,
                                         PaymentTransaction transaction, PaymentIntent paymentIntent,
                                         PaymentMethod paymentMethod, double amount, String currency) {
        return PaymentResultDTO.builder()
                .success(true)
                .message(String.format("Payment successful. Base: %.2f, Penalty: %.2f, Total: %.2f",
                        installment.getBaseTotalAmount(),
                        installment.getPenaltyAmount(),
                        amount))
                .transactionId(transaction.getId().toString())
                .paymentIntentId(paymentIntent.getId())
                .installmentId(installment.getId())
                .installmentNumber(installment.getInstallmentNumber())
                .baseAmount(installment.getBaseTotalAmount())
                .penaltyAmount(installment.getPenaltyAmount())
                .totalAmount(amount)
                .delayDays(installment.getDelayDays())
                .penaltyPercentage(installment.getPenaltyPercentage())
                .currency(currency.toUpperCase())
                .status("succeeded")
                .installmentStatus(paidInstallment.getStatus().toString())
                .cardBrand(paymentMethod.getCard().getBrand())
                .cardLast4(paymentMethod.getCard().getLast4())
                .receiptUrl(getReceiptUrl(paymentIntent))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String getReceiptUrl(PaymentIntent paymentIntent) {
        try {
            if (paymentIntent.getLatestCharge() != null) {
                com.stripe.model.Charge charge = com.stripe.model.Charge.retrieve(
                        paymentIntent.getLatestCharge());
                return charge.getReceiptUrl();
            }
        } catch (Exception e) {
            log.warn("Could not get receipt URL: {}", e.getMessage());
        }
        return null;
    }
}