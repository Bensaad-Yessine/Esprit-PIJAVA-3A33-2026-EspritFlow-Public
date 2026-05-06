package piJava.services;

import piJava.entities.Achievement;
import piJava.entities.user;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

/**
 * StreakService - Core business logic for streak management.
 * 
 * RULES:
 *  1. User gets +1 streak ONLY if they complete at least 1 course per day
 *  2. Max 1 increment per day (prevent spam on same day)
 *  3. Consecutive days → streak increases
 *  4. Miss a day → streak resets to 1
 *  5. Timezone: Africa/Tunis
 *  6. First login: current_streak = 1, longest_streak = 1
 */
public class StreakService {

    private static final String TIMEZONE = "Africa/Tunis";
    private static final ZoneId ZONE_ID = ZoneId.of(TIMEZONE);

    private final MyDataBase db;
    private final UserCourseService userCourseService;
    private final AchievementService achievementService;

    public StreakService() {
        this.db = MyDataBase.getInstance();
        this.userCourseService = new UserCourseService();
        this.achievementService = new AchievementService();
    }

    // ── Main Streak Update Logic ────────────────────────────

    /**
     * Update user streak based on course completion.
     * Called automatically when user completes a course.
     * 
     * Returns true if streak was updated, false if already processed today.
     */
    public boolean updateStreakOnCompletion(int userId) {
        try {
            user u = getUserData(userId);
            if (u == null) return false;

            LocalDate today = LocalDate.now(ZONE_ID);
            LocalDate lastStreakDate = u.getLastStreakDate() != null 
                                       ? LocalDate.parse(u.getLastStreakDate(), java.time.format.DateTimeFormatter.ISO_DATE)
                                       : null;

            // ── Anti-spam: Prevent multiple increments on the same day ──
            if (lastStreakDate != null && lastStreakDate.equals(today)) {
                System.out.println("[StreakService] Streak already updated today for user " + userId);
                return false;
            }

            // ── Check if user completed at least 1 course today ──
            int completionsToday = userCourseService.getCompletionsOnDate(userId, today);
            if (completionsToday == 0) {
                System.out.println("[StreakService] No completions today, streak not updated");
                return false;
            }

            // ── Calculate new streak ──
            int newStreak = calculateNewStreak(u, today, lastStreakDate);

            // ── Update in database ──
            String sql = "UPDATE user SET current_streak = ?, longest_streak = ?, " +
                        "last_streak_date = ?, total_courses_completed = ? WHERE id = ?";

            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, newStreak);
                stmt.setInt(2, Math.max(u.getCurrentStreak(), newStreak));
                stmt.setDate(3, java.sql.Date.valueOf(today));
                stmt.setInt(4, userCourseService.getTotalCompletedCount(userId));
                stmt.setInt(5, userId);
                
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("[StreakService] Updated streak for user " + userId + 
                                     ": new streak = " + newStreak);
                    
                    // ── Check for achievements ──
                    checkAndAwardAchievements(userId, newStreak);
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[StreakService] Error updating streak: " + e.getMessage());
        }

        return false;
    }

    public boolean updateStreakOnEnrollment(int userId) {
        try {
            user u = getUserData(userId);
            if (u == null) return false;

            LocalDate today = LocalDate.now(ZONE_ID);
            LocalDate lastStreakDate = u.getLastStreakDate() != null 
                                       ? LocalDate.parse(u.getLastStreakDate(), java.time.format.DateTimeFormatter.ISO_DATE)
                                       : null;

            // ── Anti-spam: Prevent multiple increments on the same day ──
            if (lastStreakDate != null && lastStreakDate.equals(today)) {
                System.out.println("[StreakService] Streak already updated today for user " + userId);
                return false;
            }

            // ── Calculate new streak ──
            int newStreak = calculateNewStreak(u, today, lastStreakDate);

            // ── Update in database ──
            String sql = "UPDATE user SET current_streak = ?, longest_streak = ?, " +
                        "last_streak_date = ? WHERE id = ?";

            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, newStreak);
                stmt.setInt(2, Math.max(u.getCurrentStreak(), newStreak));
                stmt.setDate(3, java.sql.Date.valueOf(today));
                stmt.setInt(4, userId);
                
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    System.out.println("[StreakService] Updated streak for user " + userId + 
                                     ": new streak = " + newStreak);
                    
                    // ── Check for achievements ──
                    checkAndAwardAchievements(userId, newStreak);
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[StreakService] Error updating streak: " + e.getMessage());
        }

        return false;
    }
    
    // ── Streak Calculation Logic ────────────────────────────

    /**
     * Calculate streak based on previous data and completion history.
     * 
     * Logic:
     *  - If no last streak date: streak = 1 (first day)
     *  - If yesterday: streak = lastStreak + 1 (consecutive)
     *  - If gap > 1 day: streak = 1 (broken, reset)
     *  - If same day: no change (prevented by anti-spam)
     */
    private int calculateNewStreak(user u, LocalDate today, LocalDate lastStreakDate) {
        int currentStreak = u.getCurrentStreak();

        // First time ever (no previous streak)
        if (lastStreakDate == null) {
            return 1;
        }

        // Calculate days elapsed
        long daysElapsed = ChronoUnit.DAYS.between(lastStreakDate, today);

        if (daysElapsed == 1) {
            // Consecutive day → increment streak
            return currentStreak + 1;
        } else if (daysElapsed > 1) {
            // Gap of > 1 day → reset to 1
            System.out.println("[StreakService] Streak broken for user (gap of " + daysElapsed + 
                             " days). Resetting to 1.");
            return 1;
        }

        // This shouldn't happen due to anti-spam, but return as-is
        return currentStreak;
    }

    // ── Achievement Checks ──────────────────────────────────

    /**
     * Check if user earned any achievements and award them.
     */
    private void checkAndAwardAchievements(int userId, int currentStreak) {
        try {
            // 7-day streak
            if (currentStreak == 7) {
                achievementService.awardAchievementIfNotEarned(userId, 
                    Achievement.AchievementType.STREAK_7);
            }

            // 30-day streak
            if (currentStreak == 30) {
                achievementService.awardAchievementIfNotEarned(userId, 
                    Achievement.AchievementType.STREAK_30);
            }

            // First course
            if (userCourseService.getTotalCompletedCount(userId) == 1) {
                achievementService.awardAchievementIfNotEarned(userId, 
                    Achievement.AchievementType.FIRST_COURSE);
            }

            // 10 courses
            if (userCourseService.getTotalCompletedCount(userId) == 10) {
                achievementService.awardAchievementIfNotEarned(userId, 
                    Achievement.AchievementType.COURSE_MASTER);
            }

        } catch (Exception e) {
            System.err.println("[StreakService] Error checking achievements: " + e.getMessage());
        }
    }

    // ── Streak Reset (Manual) ──────────────────────────────

    /**
     * Manually reset user's streak to 1.
     * Called if user is inactive or by admin.
     */
    public boolean resetStreak(int userId) {
        String sql = "UPDATE user SET current_streak = 1 WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StreakService] Error resetting streak: " + e.getMessage());
        }

        return false;
    }

    // ── Streak Retrieval ───────────────────────────────────

    /**
     * Get current streak for a user.
     */
    public int getCurrentStreak(int userId) {
        user u = getUserData(userId);
        return u != null ? u.getCurrentStreak() : 0;
    }

    /**
     * Get longest streak for a user.
     */
    public int getLongestStreak(int userId) {
        user u = getUserData(userId);
        return u != null ? u.getLongestStreak() : 0;
    }

    /**
     * Get last streak update date.
     */
    public LocalDate getLastStreakDate(int userId) {
        user u = getUserData(userId);
        if (u != null && u.getLastStreakDate() != null) {
            return LocalDate.parse(u.getLastStreakDate(), 
                java.time.format.DateTimeFormatter.ISO_DATE);
        }
        return null;
    }

    /**
     * Get days until streak is lost (if user doesn't complete a course).
     */
    public int getDaysUntilStreakLost(int userId) {
        LocalDate lastDate = getLastStreakDate(userId);
        if (lastDate == null) return 0;

        LocalDate today = LocalDate.now(ZONE_ID);
        long daysSinceLast = ChronoUnit.DAYS.between(lastDate, today);
        
        // User needs to complete 1 course within 1 day of last completion
        return Math.max(0, (int)(2 - daysSinceLast)); // 2 because streak resets after 1 day gap
    }

    /**
     * Check if user is at risk of losing streak (no completion yesterday).
     */
    public boolean isStreakAtRisk(int userId) {
        LocalDate lastDate = getLastStreakDate(userId);
        if (lastDate == null) return false;

        LocalDate yesterday = LocalDate.now(ZONE_ID).minusDays(1);
        return !lastDate.equals(yesterday);
    }

    // ── Database Helpers ───────────────────────────────────

    /**
     * Get user data (includes streak fields).
     */
    private user getUserData(int userId) {
        String sql = "SELECT id, current_streak, longest_streak, last_streak_date, " +
                    "total_courses_completed FROM user WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user u = new user();
                u.setId(rs.getInt("id"));
                u.setCurrentStreak(rs.getInt("current_streak"));
                u.setLongestStreak(rs.getInt("longest_streak"));
                
                Date lastStreakDate = rs.getDate("last_streak_date");
                if (lastStreakDate != null) {
                    u.setLastStreakDate(lastStreakDate.toString());
                }
                
                u.setTotalCoursesCompleted(rs.getInt("total_courses_completed"));
                
                return u;
            }
        } catch (SQLException e) {
            System.err.println("[StreakService] Error retrieving user data: " + e.getMessage());
        }

        return null;
    }

    // ── Helper class modifications needed in user.java ──────
    // Make sure user.java has these fields and getters:
    //  - private int currentStreak;
    //  - private int longestStreak;
    //  - private String lastStreakDate;
    //  - private int totalCoursesCompleted;
    //  - Corresponding getters and setters

    // ── Leaderboard ────────────────────────────────────────

    /**
     * Returns the top N users ordered by current_streak DESC.
     * Only active (not banned, verified) users with streak >= 1.
     *
     * @param limit max rows to return (e.g. 10)
     * @return ordered list; rank 1 = index 0
     */
    public java.util.List<user> getTopStreakUsers(int limit) {
        java.util.List<user> result = new java.util.ArrayList<>();

        String sql = "SELECT id, nom, prenom, email, roles, " +
                     "current_streak, longest_streak, last_streak_date " +
                     "FROM `user` " +
                     "WHERE current_streak >= 1 AND is_banned = 0 AND is_verified = 1 " +
                     "ORDER BY current_streak DESC " +
                     "LIMIT ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                user u = new user();
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setRoles(rs.getString("roles"));
                u.setCurrentStreak(rs.getInt("current_streak"));
                u.setLongestStreak(rs.getInt("longest_streak"));
                Date d = rs.getDate("last_streak_date");
                if (d != null) u.setLastStreakDate(d.toString());
                result.add(u);
            }
        } catch (SQLException e) {
            System.err.println("[StreakService] getTopStreakUsers error: " + e.getMessage());
        }

        return result;
    }
}

