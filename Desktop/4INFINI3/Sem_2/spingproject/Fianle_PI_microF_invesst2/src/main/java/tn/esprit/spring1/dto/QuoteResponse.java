package tn.esprit.spring1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.spring1.entities.Quote;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuoteResponse {

    private Quote quote;
    private double score;
    private String reason;
    private double successProbability;

    private byte[] investorPdf;

    // 🔥 constructeur utilisé dans ton service
    public QuoteResponse(Quote quote, double score, String reason, double successProbability) {
        this.quote = quote;
        this.score = score;
        this.reason = reason;
        this.successProbability = successProbability;
    }

    // ================== GETTERS ==================
    public Quote getQuote() {
        return quote;
    }

    public double getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }

    public double getSuccessProbability() {
        return successProbability;
    }

    public byte[] getInvestorPdf() {
        return investorPdf;
    }

    // ================== SETTERS ==================
    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setSuccessProbability(double successProbability) {
        this.successProbability = successProbability;
    }

    public void setInvestorPdf(byte[] investorPdf) {
        this.investorPdf = investorPdf;
    }
}