package piJava.entities;

import java.time.LocalDateTime;

/**
 * Achievement - User badges and accomplishments.
 */
public class Achievement {
    public enum AchievementType {
        FIRST_COURSE("first_course", "Debut", "Completez votre premier cours"),
        STREAK_7("streak_7", "7 Jours", "Maintenez une streak de 7 jours"),
        STREAK_30("streak_30", "30 Jours", "Maintenez une streak de 30 jours"),
        COURSE_MASTER("course_master", "Expert", "Completez 10 cours"),
        CATEGORY_EXPERT("category_expert", "Maitre de Categorie", "Completez 5 cours dans la meme categorie"),
        LEARNING_STREAK("learning_streak", "Apprentissage Constant", "Maintenez une streak de 14 jours");

        private final String typeId;
        private final String title;
        private final String description;

        AchievementType(String typeId, String title, String description) {
            this.typeId = typeId;
            this.title = title;
            this.description = description;
        }

        public String getTypeId() { return typeId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    private int id;
    private int userId;
    private String achievementType;
    private String title;
    private String description;
    private String badgeIcon;
    private LocalDateTime earnedAt;
    private String metadata; // JSON string for additional data

    // ─── Constructors ───────────────────────────────────────
    public Achievement() {}

    public Achievement(int userId, AchievementType type) {
        this.userId = userId;
        this.achievementType = type.getTypeId();
        this.title = type.getTitle();
        this.description = type.getDescription();
        this.earnedAt = LocalDateTime.now();
    }

    public Achievement(int userId, String achievementType, String title, 
                      String description, String badgeIcon) {
        this.userId = userId;
        this.achievementType = achievementType;
        this.title = title;
        this.description = description;
        this.badgeIcon = badgeIcon;
        this.earnedAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ──────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAchievementType() { return achievementType; }
    public void setAchievementType(String achievementType) { this.achievementType = achievementType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBadgeIcon() { return badgeIcon; }
    public void setBadgeIcon(String badgeIcon) { this.badgeIcon = badgeIcon; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    @Override
    public String toString() {
        return "Achievement{" +
                "title='" + title + '\'' +
                ", earnedAt=" + earnedAt +
                '}';
    }
}

