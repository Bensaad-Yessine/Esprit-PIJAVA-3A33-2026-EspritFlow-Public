package piJava.entities;

import java.time.LocalDateTime;

/**
 * UserInterest - Track user interests by category for recommendation system.
 */
public class UserInterest {
    private int id;
    private int userId;
    private String category;
    private int interestScore;
    private LocalDateTime lastEngagement;

    // ─── Constructors ───────────────────────────────────────
    public UserInterest() {}

    public UserInterest(int userId, String category) {
        this.userId = userId;
        this.category = category;
        this.interestScore = 1;
        this.lastEngagement = LocalDateTime.now();
    }

    public UserInterest(int userId, String category, int interestScore) {
        this(userId, category);
        this.interestScore = interestScore;
    }

    // ─── Getters & Setters ──────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getInterestScore() { return interestScore; }
    public void setInterestScore(int interestScore) { this.interestScore = interestScore; }

    public LocalDateTime getLastEngagement() { return lastEngagement; }
    public void setLastEngagement(LocalDateTime lastEngagement) { this.lastEngagement = lastEngagement; }

    // ─── Utility Methods ────────────────────────────────────
    public void incrementScore(int amount) {
        this.interestScore += amount;
        this.lastEngagement = LocalDateTime.now();
    }

    public void updateEngagement() {
        incrementScore(1);
    }

    @Override
    public String toString() {
        return category + " (score=" + interestScore + ")";
    }
}

