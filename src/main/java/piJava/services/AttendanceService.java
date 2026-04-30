package piJava.services;

import piJava.entities.Attendance;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceService implements ICrud<Attendance> {

    private Connection con;

    public AttendanceService() {
        con = MyDataBase.getInstance().getConnection();
        createTableIfNotExists();
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable.");
        }
        return con;
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS attendance (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "seance_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "scanned_at DATETIME, " +
                "FOREIGN KEY (seance_id) REFERENCES seance(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ")";
        try (Statement st = requireConnection().createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating attendance table: " + e.getMessage());
        }
    }

    @Override
    public List<Attendance> show() throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance";
        try (Statement st = requireConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public void add(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (seance_id, user_id, status, scanned_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attendance.getSeanceId());
            ps.setInt(2, attendance.getUserId());
            ps.setString(3, attendance.getStatus());
            ps.setTimestamp(4, attendance.getScannedAt());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    attendance.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void edit(Attendance attendance) throws SQLException {
        String sql = "UPDATE attendance SET seance_id = ?, user_id = ?, status = ?, scanned_at = ? WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, attendance.getSeanceId());
            ps.setInt(2, attendance.getUserId());
            ps.setString(3, attendance.getStatus());
            ps.setTimestamp(4, attendance.getScannedAt());
            ps.setInt(5, attendance.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Attendance getBySeanceAndUser(int seanceId, int userId) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE seance_id = ? AND user_id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, seanceId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Attendance> getBySeanceId(int seanceId) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE seance_id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, seanceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    private Attendance mapResultSet(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("id"),
                rs.getInt("seance_id"),
                rs.getInt("user_id"),
                rs.getString("status"),
                rs.getTimestamp("scanned_at")
        );
    }
}
