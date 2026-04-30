package piJava.entities;

import java.time.LocalDateTime;

public class Vote {

    private int id;
    private int userId;
    private int propositionId;
    private String type; // "pour", "contre", "abstention"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Full Constructor ───────────────────────────────────────
    public Vote(int id, int userId, int propositionId, String type, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.propositionId = propositionId;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─── Minimal Constructor ────────────────────────────────────
    public Vote(int userId, int propositionId, String type) {
        this.userId = userId;
        this.propositionId = propositionId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Empty Constructor ───────────────────────────────────────
    public Vote() {
    }

    // ─── Getters & Setters ───────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPropositionId() {
        return propositionId;
    }

    public void setPropositionId(int propositionId) {
        this.propositionId = propositionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", userId=" + userId +
                ", propositionId=" + propositionId +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
