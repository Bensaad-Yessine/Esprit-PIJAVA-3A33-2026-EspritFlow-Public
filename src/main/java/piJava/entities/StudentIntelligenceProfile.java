package piJava.entities;

import java.sql.Timestamp;
import java.util.Map;

public class StudentIntelligenceProfile {

    private Integer id;
    private Integer userId;
    private Timestamp analyzedAt;
    private Double abandonmentRate;
    private Double completionRate;
    private Double averageStartDelayMinutes;
    private Double averageCompletionDurationMinutes;
    private Double pauseFrequency;
    private Integer mostProductiveHour;
    private Integer mostProductiveDayOfWeek;
    private Map<String, Double> abandonmentRateByType;
    private Map<String, Double> completionRateByType;
    private Map<String, Double> averageStartDelayByType;
    private Double globalRiskTrend;
    private Double forgottenTaskProbability;
    private Double overdueProbability;
    private String weeklyProductivitySummary;
    private String behavioralAdvice;

    // Constructors
    public StudentIntelligenceProfile() {}
    public StudentIntelligenceProfile(Integer userId, Timestamp analyzedAt) {
        this.userId = userId;
        this.analyzedAt = analyzedAt;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(Timestamp analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public Double getAbandonmentRate() {
        return abandonmentRate;
    }

    public void setAbandonmentRate(Double abandonmentRate) {
        this.abandonmentRate = abandonmentRate;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Double getAverageStartDelayMinutes() {
        return averageStartDelayMinutes;
    }

    public void setAverageStartDelayMinutes(Double averageStartDelayMinutes) {
        this.averageStartDelayMinutes = averageStartDelayMinutes;
    }

    public Double getAverageCompletionDurationMinutes() {
        return averageCompletionDurationMinutes;
    }

    public void setAverageCompletionDurationMinutes(Double averageCompletionDurationMinutes) {
        this.averageCompletionDurationMinutes = averageCompletionDurationMinutes;
    }

    public Double getPauseFrequency() {
        return pauseFrequency;
    }

    public void setPauseFrequency(Double pauseFrequency) {
        this.pauseFrequency = pauseFrequency;
    }

    public Integer getMostProductiveHour() {
        return mostProductiveHour;
    }

    public void setMostProductiveHour(Integer mostProductiveHour) {
        this.mostProductiveHour = mostProductiveHour;
    }

    public Integer getMostProductiveDayOfWeek() {
        return mostProductiveDayOfWeek;
    }

    public void setMostProductiveDayOfWeek(Integer mostProductiveDayOfWeek) {
        this.mostProductiveDayOfWeek = mostProductiveDayOfWeek;
    }

    public Map<String, Double> getAbandonmentRateByType() {
        return abandonmentRateByType;
    }

    public void setAbandonmentRateByType(Map<String, Double> abandonmentRateByType) {
        this.abandonmentRateByType = abandonmentRateByType;
    }

    public Map<String, Double> getCompletionRateByType() {
        return completionRateByType;
    }

    public void setCompletionRateByType(Map<String, Double> completionRateByType) {
        this.completionRateByType = completionRateByType;
    }

    public Map<String, Double> getAverageStartDelayByType() {
        return averageStartDelayByType;
    }

    public void setAverageStartDelayByType(Map<String, Double> averageStartDelayByType) {
        this.averageStartDelayByType = averageStartDelayByType;
    }

    public Double getGlobalRiskTrend() {
        return globalRiskTrend;
    }

    public void setGlobalRiskTrend(Double globalRiskTrend) {
        this.globalRiskTrend = globalRiskTrend;
    }

    public Double getForgottenTaskProbability() {
        return forgottenTaskProbability;
    }

    public void setForgottenTaskProbability(Double forgottenTaskProbability) {
        this.forgottenTaskProbability = forgottenTaskProbability;
    }

    public Double getOverdueProbability() {
        return overdueProbability;
    }

    public void setOverdueProbability(Double overdueProbability) {
        this.overdueProbability = overdueProbability;
    }

    public String getWeeklyProductivitySummary() {
        return weeklyProductivitySummary;
    }

    public void setWeeklyProductivitySummary(String weeklyProductivitySummary) {
        this.weeklyProductivitySummary = weeklyProductivitySummary;
    }

    public String getBehavioralAdvice() {
        return behavioralAdvice;
    }

    public void setBehavioralAdvice(String behavioralAdvice) {
        this.behavioralAdvice = behavioralAdvice;
    }

    @Override
    public String toString() {
        return "StudentIntelligenceProfile{" +
                "id=" + id +
                ", userId=" + userId +
                ", analyzedAt=" + analyzedAt +
                ", abandonmentRate=" + abandonmentRate +
                ", completionRate=" + completionRate +
                ", averageStartDelayMinutes=" + averageStartDelayMinutes +
                ", averageCompletionDurationMinutes=" + averageCompletionDurationMinutes +
                ", pauseFrequency=" + pauseFrequency +
                ", mostProductiveHour=" + mostProductiveHour +
                ", mostProductiveDayOfWeek=" + mostProductiveDayOfWeek +
                ", abandonmentRateByType=" + abandonmentRateByType +
                ", completionRateByType=" + completionRateByType +
                ", averageStartDelayByType=" + averageStartDelayByType +
                ", globalRiskTrend=" + globalRiskTrend +
                ", forgottenTaskProbability=" + forgottenTaskProbability +
                ", overdueProbability=" + overdueProbability +
                ", weeklyProductivitySummary='" + weeklyProductivitySummary + '\'' +
                ", behavioralAdvice='" + behavioralAdvice + '\'' +
                '}';
    }
}