package piJava.services;

import piJava.entities.Course;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseCacheService - Store fetched courses locally for offline access.
 */
public class CourseCacheService {

    private final MyDataBase db;
    private static final long CACHE_VALIDITY_HOURS = 24;

    public CourseCacheService() {
        this.db = MyDataBase.getInstance();
    }

    /**
     * Cache a course locally.
     */
    public int cacheourse(Course course) {
        String sql = "INSERT INTO course_cache " +
                    "(api_id, title, description, category, coupon_code, expiration_date, " +
                    "course_url, instructor, rating, students_enrolled, thumbnail_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE cached_at = NOW()";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, course.getApiId());
            stmt.setString(2, course.getTitle());
            stmt.setString(3, course.getDescription());
            stmt.setString(4, course.getCategory());
            stmt.setString(5, course.getCouponCode());
            stmt.setDate(6, course.getExpirationDate() != null 
                           ? java.sql.Date.valueOf(course.getExpirationDate()) : null);
            stmt.setString(7, course.getCourseUrl());
            stmt.setString(8, course.getInstructor());
            stmt.setDouble(9, course.getRating());
            stmt.setInt(10, course.getStudentsEnrolled());
            stmt.setString(11, course.getThumbnailUrl());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[CourseCacheService] Error caching course: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Get cached course by ID.
     */
    public Course getCachedCourse(int id) {
        String sql = "SELECT * FROM course_cache WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[CourseCacheService] Error retrieving cached course: " + 
                             e.getMessage());
        }

        return null;
    }

    /**
     * Get all cached courses in a category.
     */
    public List<Course> getCoursesByCategory(String category) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course_cache WHERE LOWER(category) = LOWER(?) " +
                    "ORDER BY cached_at DESC LIMIT 50";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courses.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CourseCacheService] Error retrieving courses: " + 
                             e.getMessage());
        }

        return courses;
    }

    /**
     * Search cached courses by title.
     */
    public List<Course> searchCourses(String query) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course_cache WHERE " +
                    "LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?) " +
                    "ORDER BY rating DESC, students_enrolled DESC LIMIT 50";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courses.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CourseCacheService] Error searching courses: " + 
                             e.getMessage());
        }

        return courses;
    }

    /**
     * Get top courses by students enrolled.
     */
    public List<Course> getTopCourses(int limit) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM course_cache ORDER BY students_enrolled DESC, " +
                    "rating DESC LIMIT ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                courses.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CourseCacheService] Error retrieving top courses: " + 
                             e.getMessage());
        }

        return courses;
    }

    /**
     * Delete old cached courses (older than CACHE_VALIDITY_HOURS).
     */
    public int cleanOldCache() {
        String sql = "DELETE FROM course_cache WHERE " +
                    "TIMESTAMPDIFF(HOUR, cached_at, NOW()) > ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, CACHE_VALIDITY_HOURS);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[CourseCacheService] Error cleaning cache: " + 
                             e.getMessage());
        }

        return 0;
    }

    private Course mapResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String apiId = rs.getString("api_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String category = rs.getString("category");
        String couponCode = rs.getString("coupon_code");
        
        java.sql.Date expDate = rs.getDate("expiration_date");
        LocalDate expirationDate = expDate != null ? expDate.toLocalDate() : null;
        
        String courseUrl = rs.getString("course_url");
        String instructor = rs.getString("instructor");
        double rating = rs.getDouble("rating");
        int studentsEnrolled = rs.getInt("students_enrolled");
        String thumbnailUrl = rs.getString("thumbnail_url");

        Course c = new Course(id, apiId, title, description, category, 
                            couponCode, expirationDate, courseUrl, 
                            instructor, rating, studentsEnrolled, thumbnailUrl);
        
        Timestamp ts = rs.getTimestamp("cached_at");
        if (ts != null) {
            c.setCachedAt(ts.getTime());
        }

        return c;
    }
}


