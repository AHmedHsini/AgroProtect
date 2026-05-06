package tn.esprit.scoringaideservice.dto;

public class MlRequest {

    private Double agronomique;
    private Double climatique;
    private Double productivite;
    private Double stabilite;
    private Double market;

    // GETTERS
    public Double getAgronomique() {
        return agronomique;
    }

    public Double getClimatique() {
        return climatique;
    }

    public Double getProductivite() {
        return productivite;
    }

    public Double getStabilite() {
        return stabilite;
    }

    public Double getMarket() {
        return market;
    }

    // SETTERS
    public void setAgronomique(Double agronomique) {
        this.agronomique = agronomique;
    }

    public void setClimatique(Double climatique) {
        this.climatique = climatique;
    }

    public void setProductivite(Double productivite) {
        this.productivite = productivite;
    }

    public void setStabilite(Double stabilite) {
        this.stabilite = stabilite;
    }

    public void setMarket(Double market) {
        this.market = market;
    }
}