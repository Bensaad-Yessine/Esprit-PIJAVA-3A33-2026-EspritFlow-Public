package piJava.services;

import piJava.entities.*;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * UserCourseService - Manage user course enrollment and completion tracking.
 * 
 * Responsibilities:
 *  - Enroll user in course
 *  - Mark course as completed
 *  - Prevent duplicate completions
 *  - Track progress percentage
 *  - Retrieve user's courses
 */
public class UserCourseService {

    private final MyDataBase db;

    public UserCourseService() {
        this.db = MyDataBase.getInstance();
    }

    // ── Enrollment ────────────────────────────────────────────

    /**
     * Enroll a user in a course (or return existing enrollment).
     * @return UserCourse entity with ID
     */
    public UserCourse enrollUserInCourse(int userId, int courseId, String apiCourseId) {
        try {
            // Check if already enrolled
            UserCourse existing = getUserCourse(userId, courseId);
            if (existing != null) {
                return existing;
            }

            String sql = "INSERT INTO user_course (user_id, course_id, api_course_id, enrolled_date) " +
                        "VALUES (?, ?, ?, NOW())";

            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, courseId);
                stmt.setString(3, apiCourseId);
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    UserCourse uc = new UserCourse(userId, courseId, apiCourseId);
                    uc.setId(keys.getInt(1));
                    uc.setEnrolledDate(LocalDateTime.now());
                    return uc;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error enrolling user: " + e.getMessage());
        }

        return null;
    }

    /**
     * Mark a course as completed by the user.
     * Anti-spam: prevents duplicate completion on the same day.
     */
    public boolean markCourseAsCompleted(int userId, int courseId) {
        try {
            // Check if NOT already completed today
            UserCourse existing = getUserCourse(userId, courseId);
            if (existing != null && existing.isCompleted()) {
                // Already completed, check if it's the same day
                if (existing.getCompletionDate() != null) {
                    LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                    if (existing.getCompletionDate().isAfter(today)) {
                        System.out.println("[UserCourseService] Course already completed today, preventing duplicate");
                        return false;
                    }
                }
            }

            String sql = "UPDATE user_course SET is_completed = TRUE, completion_date = NOW(), " +
                        "progress_percentage = 100, last_accessed = NOW() " +
                        "WHERE user_id = ? AND course_id = ?";

            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, courseId);
                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error marking course complete: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update progress percentage for a course.
     */
    public boolean updateProgress(int userId, int courseId, int progressPercentage) {
        if (progressPercentage < 0 || progressPercentage > 100) {
            return false;
        }

        try {
            String sql = "UPDATE user_course SET progress_percentage = ?, last_accessed = NOW() " +
                        "WHERE user_id = ? AND course_id = ?";

            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, progressPercentage);
                stmt.setInt(2, userId);
                stmt.setInt(3, courseId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error updating progress: " + e.getMessage());
        }

        return false;
    }

    // ── Retrieval ─────────────────────────────────────────────

    /**
     * Get a specific user course enrollment.
     */
    public UserCourse getUserCourse(int userId, int courseId) {
        String sql = "SELECT * FROM user_course WHERE user_id = ? AND course_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error retrieving user course: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all courses for a user.
     */
    public List<UserCourse> getUserCourses(int userId) {
        List<UserCourse> courses = new ArrayList<>();
        String sql = "SELECT * FROM user_course WHERE user_id = ? ORDER BY enrolled_date DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courses.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error retrieving user courses: " + e.getMessage());
        }

        return courses;
    }

    /**
     * Get completed courses for a user.
     */
    public List<UserCourse> getCompletedCourses(int userId) {
        List<UserCourse> courses = new ArrayList<>();
        String sql = "SELECT * FROM user_course WHERE user_id = ? AND is_completed = TRUE " +
                    "ORDER BY completion_date DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courses.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error retrieving completed courses: " + e.getMessage());
        }

        return courses;
    }

    /**
     * Get courses completed in the last N days.
     */
    public List<UserCourse> getRecentCompletions(int userId, int daysBack) {
        List<UserCourse> courses = new ArrayList<>();
        String sql = "SELECT * FROM user_course WHERE user_id = ? AND is_completed = TRUE " +
                    "AND completion_date >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                    "ORDER BY completion_date DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, daysBack);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courses.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error retrieving recent completions: " + e.getMessage());
        }

        return courses;
    }

    /**
     * Get courses completed on a specific date (for streak calculation).
     */
    public int getCompletionsOnDate(int userId, java.time.LocalDate date) {
        String sql = "SELECT COUNT(*) as count FROM user_course " +
                    "WHERE user_id = ? AND is_completed = TRUE AND DATE(completion_date) = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error counting completions: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get total courses completed by user.
     */
    public int getTotalCompletedCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM user_course WHERE user_id = ? AND is_completed = TRUE";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error counting completions: " + e.getMessage());
        }

        return 0;
    }

    // ── Utility ────────────────────────────────────────────────

    /**
     * Map ResultSet to UserCourse entity.
     */
    private UserCourse mapResultSet(ResultSet rs) throws SQLException {
        UserCourse uc = new UserCourse();
        uc.setId(rs.getInt("id"));
        uc.setUserId(rs.getInt("user_id"));
        uc.setCourseId(rs.getInt("course_id"));
        uc.setApiCourseId(rs.getString("api_course_id"));
        uc.setCompleted(rs.getBoolean("is_completed"));
        
        Timestamp completionTs = rs.getTimestamp("completion_date");
        if (completionTs != null) {
            uc.setCompletionDate(completionTs.toLocalDateTime());
        }
        
        uc.setProgressPercentage(rs.getInt("progress_percentage"));
        
        Timestamp enrolledTs = rs.getTimestamp("enrolled_date");
        if (enrolledTs != null) {
            uc.setEnrolledDate(enrolledTs.toLocalDateTime());
        }
        
        Timestamp lastAccessedTs = rs.getTimestamp("last_accessed");
        if (lastAccessedTs != null) {
            uc.setLastAccessed(lastAccessedTs.toLocalDateTime());
        }
        
        uc.setNotes(rs.getString("notes"));
        
        return uc;
    }

    /**
     * Delete a user course enrollment.
     */
    public boolean deleteEnrollment(int userId, int courseId) {
        String sql = "DELETE FROM user_course WHERE user_id = ? AND course_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserCourseService] Error deleting enrollment: " + e.getMessage());
        }

        return false;
    }
}

