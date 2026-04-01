package AgroProtect.services;

import AgroProtect.DTOs.PaymentResultDTO;
import AgroProtect.entities.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:AgroProtect}")
    private String appName;

    // ===== CREDIT APPLICATION NOTIFICATIONS =====

    /**
     * Send email when credit application is ACCEPTED
     */
    public void sendApplicationAcceptedEmail(String toEmail, String farmerName,
                                             CreditApplication application,
                                             AgriculturalCredit credit) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Credit Team");
            helper.setTo(toEmail);
            helper.setSubject("🎉 Congratulations! Your Credit Application is Approved");

            String html = buildApplicationAcceptedHtml(farmerName, application, credit);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Application accepted email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send application accepted email: {}", e.getMessage());
            sendSimpleApplicationAccepted(toEmail, farmerName, application, credit);
        }
    }

    /**
     * Send email when credit application is REFUSED
     */
    public void sendApplicationRefusedEmail(String toEmail, String farmerName,
                                            CreditApplication application,
                                            String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Credit Team");
            helper.setTo(toEmail);
            helper.setSubject("Update on Your Credit Application");

            String html = buildApplicationRefusedHtml(farmerName, application, reason);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Application refused email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send application refused email: {}", e.getMessage());
        }
    }

    // ===== REPAYMENT SCHEDULE NOTIFICATIONS =====

    /**
     * Send email when repayment schedule is created with all installments
     */
    public void sendScheduleCreatedEmail(String toEmail, String farmerName,
                                         AgriculturalCredit credit,
                                         RepaymentSchedule schedule,
                                         List<Installment> installments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Credit Team");
            helper.setTo(toEmail);
            helper.setSubject("📅 Your Repayment Schedule is Ready");

            String html = buildScheduleCreatedHtml(farmerName, credit, schedule, installments);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Schedule created email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send schedule email: {}", e.getMessage());
        }
    }

    // ===== PAYMENT NOTIFICATIONS =====

    /**
     * Send payment receipt (existing)
     */
    public void sendPaymentReceipt(String toEmail, PaymentResultDTO payment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Payment System");
            helper.setTo(toEmail);
            helper.setSubject("✅ Payment Receipt - Installment #" + payment.installmentNumber);

            String html = buildReceiptHtml(payment);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Receipt email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send receipt: {}", e.getMessage());
            sendSimpleReceipt(toEmail, payment);
        }
    }

    /**
     * Send payment reminder before due date
     */
    public void sendPaymentReminder(String toEmail, String farmerName,
                                    Installment installment,
                                    double amountDue,
                                    int daysUntilDue) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName + " Payment Reminder");
            helper.setTo(toEmail);

            String subject = daysUntilDue <= 0
                    ? "⚠️ URGENT: Payment Overdue - Installment #" + installment.getInstallmentNumber()
                    : "⏰ Payment Reminder: Due in " + daysUntilDue + " days";

            helper.setSubject(subject);

            String html = buildPaymentReminderHtml(farmerName, installment, amountDue, daysUntilDue);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Payment reminder sent to: {} (due in {} days)", toEmail, daysUntilDue);

        } catch (Exception e) {
            log.error("Failed to send reminder: {}", e.getMessage());
        }
    }

    // ===== HTML BUILDERS =====

    private String buildApplicationAcceptedHtml(String farmerName,
                                                CreditApplication application,
                                                AgriculturalCredit credit) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #4CAF50, #45a049); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .success-box { background: #e8f5e9; border-left: 4px solid #4CAF50; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .details { background: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #eee; }
                    .row:last-child { border-bottom: none; }
                    .highlight { font-size: 24px; color: #4CAF50; font-weight: bold; }
                    .next-steps { background: #fff3e0; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .footer { background: #fafafa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                    .btn { display: inline-block; background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌾 %s</h1>
                        <h2>Credit Application Approved!</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <div class="success-box">
                            <h3>🎉 Congratulations!</h3>
                            <p>Your credit application has been <strong>APPROVED</strong>. We're excited to support your agricultural project!</p>
                        </div>
                        
                        <div class="details">
                            <h3>Credit Details</h3>
                            <div class="row">
                                <span>Approved Amount:</span>
                                <span class="highlight">%.2f TND</span>
                            </div>
                            <div class="row">
                                <span>Duration:</span>
                                <span>%d months</span>
                            </div>
                            <div class="row">
                                <span>Interest Rate:</span>
                                <span>%.2f%%</span>
                            </div>
                            <div class="row">
                                <span>Total Repayment:</span>
                                <span>%.2f TND</span>
                            </div>
                            <div class="row">
                                <span>Monthly Payment:</span>
                                <span>~%.2f TND</span>
                            </div>
                            <div class="row">
                                <span>Credit ID:</span>
                                <span>#%d</span>
                            </div>
                        </div>
                        
                        <div class="next-steps">
                            <h3>📋 Next Steps</h3>
                            <ol>
                                <li>Your repayment schedule will be generated shortly</li>
                                <li>You'll receive another email with installment details</li>
                                <li>First payment due after grace period: <strong>%s</strong></li>
                            </ol>
                        </div>
                        
                        <p style="text-align: center;">
                            <a href="#" class="btn">View Credit Dashboard</a>
                        </p>
                        
                        <p>Thank you for choosing %s for your agricultural financing needs!</p>
                    </div>
                    
                    <div class="footer">
                        <p>This is an automated message from %s</p>
                        <p>© 2026 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                appName,
                farmerName,
                credit.getApprovedAmount(),
                credit.getDurationMonths(),
                credit.getInterestRate() * 100,
                credit.getTotalRepaymentAmount(),
                credit.getTotalRepaymentAmount() / credit.getDurationMonths(),
                credit.getIdAgriculturalCredit(),
                credit.getDisbursementDate().plusMonths(credit.getGracePeriodMonths()).toString(),
                appName,
                appName,
                appName
        );
    }

    private String buildApplicationRefusedHtml(String farmerName,
                                               CreditApplication application,
                                               String reason) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: #f44336; color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .reason-box { background: #ffebee; border-left: 4px solid #f44336; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .suggestions { background: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .footer { background: #fafafa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌾 %s</h1>
                        <h2>Credit Application Update</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Thank you for your interest in %s agricultural credit services.</p>
                        
                        <p>After careful review of your application for <strong>%.2f TND</strong>, 
                        we regret to inform you that we cannot approve your request at this time.</p>
                        
                        <div class="reason-box">
                            <h3>Reason:</h3>
                            <p>%s</p>
                        </div>
                        
                        <div class="suggestions">
                            <h3>💡 How to Improve Your Chances</h3>
                            <ul>
                                <li>Improve your credit history by repaying existing loans</li>
                                <li>Increase your documented income sources</li>
                                <li>Reduce existing debt obligations</li>
                                <li>Provide additional collateral or guarantees</li>
                                <li>Apply for a smaller amount</li>
                            </ul>
                        </div>
                        
                        <p>You can reapply after 3 months. We're here to help you succeed!</p>
                        
                        <p>Questions? Contact our support team.</p>
                    </div>
                    
                    <div class="footer">
                        <p>%s - Supporting Farmers</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                appName,
                farmerName,
                appName,
                application.getRequestedAmount(),
                reason,
                appName
        );
    }

    private String buildScheduleCreatedHtml(String farmerName,
                                            AgriculturalCredit credit,
                                            RepaymentSchedule schedule,
                                            List<Installment> installments) {
        StringBuilder installmentRows = new StringBuilder();

        for (Installment inst : installments) {
            installmentRows.append(String.format("""
                <tr>
                    <td style="padding: 10px; border-bottom: 1px solid #eee;">%d</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee;">%s</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">%.2f TND</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">%.2f TND</td>
                    <td style="padding: 10px; border-bottom: 1px solid #eee; text-align: right;">%.2f TND</td>
                </tr>
                """,
                    inst.getInstallmentNumber(),
                    inst.getDueDate().toString(),
                    inst.getPrincipalAmount(),
                    inst.getInterestAmount(),
                    inst.getTotalAmount()
            ));
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
                    .container { max-width: 700px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #2196F3, #1976D2); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .summary { background: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .installments { margin: 20px 0; }
                    table { width: 100%%; border-collapse: collapse; font-size: 14px; }
                    th { background: #f5f5f5; padding: 12px; text-align: left; font-weight: 600; }
                    .important { background: #fff3e0; border-left: 4px solid #ff9800; padding: 15px; margin: 20px 0; }
                    .footer { background: #fafafa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌾 %s</h1>
                        <h2>Your Repayment Schedule</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Your repayment schedule has been generated for Credit <strong>#%d</strong>.</p>
                        
                        <div class="summary">
                            <h3>📊 Schedule Summary</h3>
                            <p><strong>Total Credit:</strong> %.2f TND</p>
                            <p><strong>Number of Installments:</strong> %d</p>
                            <p><strong>First Payment Due:</strong> %s</p>
                            <p><strong>Final Payment Due:</strong> %s</p>
                            <p><strong>Payment Frequency:</strong> %s</p>
                        </div>
                        
                        <div class="installments">
                            <h3>📅 Installment Details</h3>
                            <table>
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Due Date</th>
                                        <th style="text-align: right;">Principal</th>
                                        <th style="text-align: right;">Interest</th>
                                        <th style="text-align: right;">Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    %s
                                </tbody>
                            </table>
                        </div>
                        
                        <div class="important">
                            <h3>⚠️ Important Reminders</h3>
                            <ul>
                                <li>Payments are due on the dates shown above</li>
                                <li>Late payments incur penalties: 2%% (1-30 days), 5%% (31-60 days), 10%% (61-90 days), 15%% (>90 days)</li>
                                <li>You can pay online through our secure payment system</li>
                                <li>Early repayment is accepted without penalty</li>
                            </ul>
                        </div>
                        
                        <p>Thank you for choosing %s!</p>
                    </div>
                    
                    <div class="footer">
                        <p>%s - Supporting Agricultural Growth</p>
                        <p>Questions? Contact support at %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                appName,
                farmerName,
                credit.getIdAgriculturalCredit(),
                credit.getApprovedAmount(),
                installments.size(),
                installments.get(0).getDueDate().toString(),
                installments.get(installments.size() - 1).getDueDate().toString(),
                schedule.getPaymentFrequency(),
                installmentRows.toString(),
                appName,
                appName,
                fromEmail
        );
    }

    private String buildPaymentReminderHtml(String farmerName,
                                            Installment installment,
                                            double amountDue,
                                            int daysUntilDue) {
        String urgencyColor = daysUntilDue <= 0 ? "#f44336" : (daysUntilDue <= 3 ? "#ff9800" : "#2196F3");
        String urgencyText = daysUntilDue <= 0 ? "OVERDUE" : (daysUntilDue <= 3 ? "DUE SOON" : "UPCOMING");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: %s; color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .amount-box { background: #f5f5f5; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; }
                    .amount { font-size: 36px; font-weight: bold; color: %s; }
                    .btn { display: inline-block; background: #4CAF50; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; font-size: 16px; margin: 20px 0; }
                    .footer { background: #fafafa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌾 %s</h1>
                        <h2>Payment %s</h2>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>This is a friendly reminder about your upcoming installment payment.</p>
                        
                        <div class="amount-box">
                            <p>Installment #%d</p>
                            <p class="amount">%.2f TND</p>
                            <p>Due: %s (%s)</p>
                        </div>
                        
                        <p style="text-align: center;">
                            <a href="#" class="btn">Pay Now</a>
                        </p>
                        
                        <p>Pay on time to avoid penalties and maintain good credit standing.</p>
                    </div>
                    
                    <div class="footer">
                        <p>%s - Supporting Farmers</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                urgencyColor,
                urgencyColor,
                appName,
                urgencyText,
                farmerName,
                installment.getInstallmentNumber(),
                amountDue,
                installment.getDueDate().toString(),
                daysUntilDue <= 0 ? "OVERDUE" : daysUntilDue + " days remaining",
                appName
        );
    }

    private String buildReceiptHtml(PaymentResultDTO p) {
        String dateStr = p.timestamp.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #4CAF50, #45a049); color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .success { color: #4CAF50; font-size: 18px; font-weight: bold; }
                    .row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #eee; }
                    .total { font-size: 24px; font-weight: bold; color: #4CAF50; }
                    .penalty { color: #ff9800; }
                    .footer { background: #fafafa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌾 %s</h1>
                        <h2>Payment Receipt</h2>
                    </div>
                    
                    <div class="content">
                        <p class="success">✅ Payment Successful</p>
                        
                        <div class="row">
                            <span>Installment Number:</span>
                            <strong>#%d</strong>
                        </div>
                        
                        <div class="row">
                            <span>Base Amount:</span>
                            <span>%.2f %s</span>
                        </div>
                        
                        <div class="row">
                            <span>Penalty (Late Payment):</span>
                            <span class="penalty">%.2f %s (%d days late)</span>
                        </div>
                        
                        <div class="row">
                            <span>Card Used:</span>
                            <span>%s ****%s</span>
                        </div>
                        
                        <div class="row">
                            <span>Transaction ID:</span>
                            <span>%s</span>
                        </div>
                        
                        <div class="row">
                            <span>Date:</span>
                            <span>%s</span>
                        </div>
                        
                        <hr style="margin: 20px 0;">
                        
                        <div class="row total">
                            <span>TOTAL PAID:</span>
                            <span>%.2f %s</span>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>Thank you for your payment!</p>
                        <p>%s Agricultural Credit System</p>
                        <p><small>Keep this receipt for your records</small></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                appName,
                p.installmentNumber,
                p.baseAmount, p.currency,
                p.penaltyAmount, p.currency, p.delayDays,
                p.cardBrand, p.cardLast4,
                p.transactionId,
                dateStr,
                p.totalAmount, p.currency,
                appName
        );
    }

    private void sendSimpleReceipt(String toEmail, PaymentResultDTO payment) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Payment Receipt - " + appName);

            String text = String.format("""
                %s - Payment Receipt
                
                Installment #%d
                Amount Paid: %.2f %s
                Date: %s
                
                Thank you for your payment!
                """,
                    appName,
                    payment.installmentNumber,
                    payment.totalAmount,
                    payment.currency,
                    payment.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );

            message.setText(text);
            mailSender.send(message);
            log.info("Simple receipt sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send simple email: {}", e.getMessage());
        }
    }

    private void sendSimpleApplicationAccepted(String toEmail, String farmerName,
                                               CreditApplication application,
                                               AgriculturalCredit credit) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Credit Application Approved - " + appName);

            String text = String.format("""
                Congratulations %s!
                
                Your credit application has been APPROVED.
                
                Approved Amount: %.2f TND
                Duration: %d months
                Interest Rate: %.2f%%
                Total Repayment: %.2f TND
                
                Your repayment schedule will be sent shortly.
                
                Thank you for choosing %s!
                """,
                    farmerName,
                    credit.getApprovedAmount(),
                    credit.getDurationMonths(),
                    credit.getInterestRate() * 100,
                    credit.getTotalRepaymentAmount(),
                    appName
            );

            message.setText(text);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send simple acceptance email: {}", e.getMessage());
        }
    }
}