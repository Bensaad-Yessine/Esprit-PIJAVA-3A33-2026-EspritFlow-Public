package piJava.services;

import piJava.entities.UserInterest;
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
 * Manage user interests and categories.
 */
public class UserInterestService {

    private final MyDataBase db;

    public UserInterestService() {
        this.db = MyDataBase.getInstance();
    }

    /**
     * Get or create an interest record.
     */
    public UserInterest getOrCreateInterest(int userId, String category) {
        UserInterest existing = getInterest(userId, category);

        if (existing != null) {
            existing.incrementScore(1);
            updateInterest(existing);
            return existing;
        }

        return createInterest(new UserInterest(userId, category));
    }

    /**
     * Create a new user interest.
     */
    public UserInterest createInterest(UserInterest interest) {
        String sql = "INSERT INTO user_interest (user_id, category, interest_score, last_engagement) " +
                "VALUES (?, ?, ?, NOW())";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, interest.getUserId());
            stmt.setString(2, interest.getCategory());
            stmt.setInt(3, interest.getInterestScore());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                interest.setId(keys.getInt(1));
            }
            return interest;
        } catch (SQLException e) {
            System.err.println("[UserInterestService] Error creating interest: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get a specific interest.
     */
    public UserInterest getInterest(int userId, String category) {
        String sql = "SELECT * FROM user_interest WHERE user_id = ? AND category = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, category);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserInterestService] Error retrieving interest: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get top N interests for a user.
     */
    public List<UserInterest> getTopInterests(int userId, int limit) {
        List<UserInterest> interests = new ArrayList<>();
        String sql = "SELECT * FROM user_interest WHERE user_id = ? ORDER BY interest_score DESC LIMIT ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                interests.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserInterestService] Error retrieving top interests: " + e.getMessage());
        }

        return interests;
    }

    /**
     * Update interest score.
     */
    public boolean updateInterest(UserInterest interest) {
        String sql = "UPDATE user_interest SET interest_score = ?, last_engagement = NOW() WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, interest.getInterestScore());
            stmt.setInt(2, interest.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserInterestService] Error updating interest: " + e.getMessage());
        }

        return false;
    }

    private UserInterest mapResultSet(ResultSet rs) throws SQLException {
        UserInterest ui = new UserInterest();
        ui.setId(rs.getInt("id"));
        ui.setUserId(rs.getInt("user_id"));
        ui.setCategory(rs.getString("category"));
        ui.setInterestScore(rs.getInt("interest_score"));

        Timestamp ts = rs.getTimestamp("last_engagement");
        if (ts != null) {
            ui.setLastEngagement(ts.toLocalDateTime());
        }

        return ui;
    }
}

