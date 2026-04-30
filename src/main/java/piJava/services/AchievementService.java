package piJava.services;

import piJava.entities.Achievement;
import piJava.entities.Achievement.AchievementType;
import piJava.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Award and track user achievements/badges.
 */
public class AchievementService {

    private final MyDataBase db;

    public AchievementService() {
        this.db = MyDataBase.getInstance();
    }

    /**
     * Award an achievement to a user (if not already earned).
     */
    public Achievement awardAchievementIfNotEarned(int userId, AchievementType type) {
        if (hasEarnedAchievement(userId, type.getTypeId())) {
            return null;
        }

        Achievement achievement = new Achievement(userId, type);
        return createAchievement(achievement);
    }

    /**
     * Create/save an achievement.
     */
    public Achievement createAchievement(Achievement achievement) {
        String sql = "INSERT INTO achievement (user_id, achievement_type, title, description, earned_at) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, achievement.getUserId());
            stmt.setString(2, achievement.getAchievementType());
            stmt.setString(3, achievement.getTitle());
            stmt.setString(4, achievement.getDescription());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                achievement.setId(keys.getInt(1));
            }

            System.out.println("[AchievementService] Awarded '" + achievement.getTitle() +
                    "' to user " + achievement.getUserId());
            return achievement;
        } catch (SQLException e) {
            System.err.println("[AchievementService] Error creating achievement: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all achievements for a user.
     */
    public List<Achievement> getUserAchievements(int userId) {
        List<Achievement> achievements = new ArrayList<>();
        String sql = "SELECT * FROM achievement WHERE user_id = ? ORDER BY earned_at DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                achievements.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AchievementService] Error retrieving achievements: " + e.getMessage());
        }

        return achievements;
    }

    /**
     * Check if user has earned an achievement.
     */
    public boolean hasEarnedAchievement(int userId, String achievementType) {
        String sql = "SELECT COUNT(*) as count FROM achievement WHERE user_id = ? AND achievement_type = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, achievementType);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("[AchievementService] Error checking achievement: " + e.getMessage());
        }

        return false;
    }

    private Achievement mapResultSet(ResultSet rs) throws SQLException {
        Achievement a = new Achievement();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setAchievementType(rs.getString("achievement_type"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setBadgeIcon(rs.getString("badge_icon"));

        Timestamp ts = rs.getTimestamp("earned_at");
        if (ts != null) {
            a.setEarnedAt(ts.toLocalDateTime());
        }

        a.setMetadata(rs.getString("metadata"));
        return a;
    }
}

