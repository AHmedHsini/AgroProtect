package AgroProtect.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Value("${app.return.url:http://localhost:8083/AgroProtect/payments/return}")
    private String returnUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe initialized");
    }

    public PaymentIntent createPaymentIntent(Double amount, String currency,
                                             Long installmentId, String description,
                                             String customerEmail) throws StripeException {

        long amountInCents = Math.round(amount * 100);

        PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase())
                .setDescription(description)
                .putMetadata("installment_id", installmentId.toString())
                .putMetadata("original_amount", String.valueOf(amount))
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build());

        // Set receipt email for automatic Stripe receipt
        if (customerEmail != null && !customerEmail.isEmpty()) {
            builder.setReceiptEmail(customerEmail);
            log.info("Receipt will be sent to: {}", customerEmail);
        }

        return PaymentIntent.create(builder.build());
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .setReturnUrl(returnUrl)
                .build();

        PaymentIntent confirmed = paymentIntent.confirm(params);

        // Force receipt generation by updating charge if needed
        if (confirmed.getLatestCharge() != null && confirmed.getReceiptEmail() != null) {
            try {
                Charge charge = Charge.retrieve(confirmed.getLatestCharge());
                if (charge.getReceiptUrl() == null) {
                    // Receipt not generated yet, request it
                    log.info("Requesting receipt for charge: {}", charge.getId());
                }
            } catch (Exception e) {
                log.warn("Could not process receipt: {}", e.getMessage());
            }
        }

        return confirmed;
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder()
                .addExpand("payment_method")
                .addExpand("charges")
                .build();

        return PaymentIntent.retrieve(paymentIntentId, params, null);
    }

    public Refund createRefund(String paymentIntentId, Double amount, String reason) throws StripeException {
        RefundCreateParams.Builder builder = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);

        if (amount != null) {
            builder.setAmount(Math.round(amount * 100));
        }

        return Refund.create(builder.build());
    }
}