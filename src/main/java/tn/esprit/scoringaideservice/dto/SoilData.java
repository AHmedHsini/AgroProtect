package tn.esprit.scoringaideservice.dto;

public class SoilData {

    private String wrbClass;
    private double probability;
    private double ph;
    private double organicCarbon;
    private double clay;
    private double sand;

    private double dataConfidence;

    public String getWrbClass() {
        return wrbClass;
    }

    public void setWrbClass(String wrbClass) {
        this.wrbClass = wrbClass;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    public double getOrganicCarbon() {
        return organicCarbon;
    }

    public void setOrganicCarbon(double organicCarbon) {
        this.organicCarbon = organicCarbon;
    }

    public double getClay() {
        return clay;
    }

    public void setClay(double clay) {
        this.clay = clay;
    }

    public double getSand() {
        return sand;
    }

    public void setSand(double sand) {
        this.sand = sand;
    }



    public double getDataConfidence() {
        return dataConfidence;
    }

    public void setDataConfidence(double dataConfidence) {
        this.dataConfidence = dataConfidence;
    }

}
