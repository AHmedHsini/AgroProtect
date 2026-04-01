package AgroProtect.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Payment result with Stripe and installment details")
public class PaymentResultDTO {

    @Schema(description = "Payment success", example = "true")
    public boolean success;

    @Schema(description = "Message", example = "Payment successful")
    public String message;

    @Schema(description = "Transaction ID", example = "123")
    public String transactionId;

    @Schema(description = "Stripe PaymentIntent ID", example = "pi_3O...")
    public String paymentIntentId;

    @Schema(description = "Installment ID", example = "14")
    public Long installmentId;

    @Schema(description = "Installment number", example = "2")
    public Integer installmentNumber;

    @Schema(description = "Base amount", example = "684.86")
    public Double baseAmount;

    @Schema(description = "Penalty amount", example = "34.24")
    public Double penaltyAmount;

    @Schema(description = "Total amount paid", example = "719.10")
    public Double totalAmount;

    @Schema(description = "Delay days", example = "59")
    public Integer delayDays;

    @Schema(description = "Penalty percentage", example = "0.05")
    public Double penaltyPercentage;

    @Schema(description = "Currency", example = "USD")
    public String currency;

    @Schema(description = "Payment status", example = "succeeded")
    public String status;

    @Schema(description = "Installment new status", example = "PAID")
    public String installmentStatus;

    @Schema(description = "Card brand", example = "visa")
    public String cardBrand;

    @Schema(description = "Card last 4 digits", example = "4242")
    public String cardLast4;

    @Schema(description = "Receipt URL", example = "https://pay.stripe.com/receipts/...")
    public String receiptUrl;

    @Schema(description = "Timestamp", example = "2026-03-31T15:05:00")
    public LocalDateTime timestamp;
}