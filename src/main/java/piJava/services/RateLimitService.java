package piJava.services;

import piJava.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Rate limiting and anti-spam service.
 */
public class RateLimitService {

    private final MyDataBase db;
    private static final int MAX_REQUESTS_PER_DAY = 100;
    private static final int MAX_COMPLETIONS_PER_DAY = 50;

    public RateLimitService() {
        this.db = MyDataBase.getInstance();
    }

    /**
     * Check if user exceeded rate limit for API requests.
     */
    public boolean isRateLimited(int userId, String requestType) {
        String sql = "SELECT request_count FROM api_request_log " +
                "WHERE user_id = ? AND request_type = ? AND DATE(last_request) = CURDATE()";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, requestType);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("request_count");
                return count >= MAX_REQUESTS_PER_DAY;
            }
        } catch (SQLException e) {
            System.err.println("[RateLimitService] Error checking rate limit: " + e.getMessage());
        }

        return false;
    }

    /**
     * Increment request counter for user.
     */
    public void recordRequest(int userId, String requestType) {
        String sql = "INSERT INTO api_request_log (user_id, request_type, request_count) " +
                "VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE request_count = request_count + 1, last_request = NOW()";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, requestType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[RateLimitService] Error recording request: " + e.getMessage());
        }
    }

    /**
     * Check if user completed too many courses today (anti-spam).
     */
    public boolean canCompleteMoreCourses(int userId) {
        UserCourseService uc = new UserCourseService();
        int completionsToday = uc.getRecentCompletions(userId, 1).size();
        return completionsToday < MAX_COMPLETIONS_PER_DAY;
    }
}

