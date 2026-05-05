package piJava.entities;

public class WorkloadStats {
    private int classeId;
    private double chargeBruteTotale;
    private double chargePonderee;
    private double indicePression;
    private String alerteNiveau; // "VERT", "ORANGE", "ROUGE"
    private String suggestion;
    private String recommendationMatiere;

    public WorkloadStats() {}

    public int getClasseId() { return classeId; }
    public void setClasseId(int classeId) { this.classeId = classeId; }

    public double getChargeBruteTotale() { return chargeBruteTotale; }
    public void setChargeBruteTotale(double chargeBruteTotale) { this.chargeBruteTotale = chargeBruteTotale; }

    public double getChargePonderee() { return chargePonderee; }
    public void setChargePonderee(double chargePonderee) { this.chargePonderee = chargePonderee; }

    public double getIndicePression() { return indicePression; }
    public void setIndicePression(double indicePression) { this.indicePression = indicePression; }

    public String getAlerteNiveau() { return alerteNiveau; }
    public void setAlerteNiveau(String alerteNiveau) { this.alerteNiveau = alerteNiveau; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getRecommendationMatiere() { return recommendationMatiere; }
    public void setRecommendationMatiere(String recommendationMatiere) { this.recommendationMatiere = recommendationMatiere; }
}

