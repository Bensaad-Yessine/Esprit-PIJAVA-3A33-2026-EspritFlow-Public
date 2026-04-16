package piJava.services;

import piJava.entities.Salle;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleService implements ICrud<Salle> {

    private Connection con;

    public SalleService() {
        con = MyDataBase.getInstance().getConnection();
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable.");
        }
        return con;
    }

    @Override
    public List<Salle> show() throws SQLException {
        return getAllSalles();
    }

    public List<Salle> getAllSalles() throws SQLException {
        List<Salle> list = new ArrayList<>();
        String sql = "SELECT * FROM salle";
        try (Statement st = requireConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public void add(Salle salle) throws SQLException {
        String sql = "INSERT INTO salle (block, number, name, etage, capacite, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, salle.getBlock());
            ps.setInt(2, salle.getNumber());
            ps.setString(3, salle.getName());
            ps.setInt(4, salle.getEtage());
            ps.setInt(5, salle.getCapacite());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    salle.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void edit(Salle salle) throws SQLException {
        String sql = "UPDATE salle SET block = ?, number = ?, name = ?, etage = ?, capacite = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, salle.getBlock());
            ps.setInt(2, salle.getNumber());
            ps.setString(3, salle.getName());
            ps.setInt(4, salle.getEtage());
            ps.setInt(5, salle.getCapacite());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.setInt(7, salle.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM salle WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Salle getById(int id) throws SQLException {
        String sql = "SELECT * FROM salle WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Salle> searchByName(String keyword) throws SQLException {
        List<Salle> list = new ArrayList<>();
        String sql = "SELECT * FROM salle WHERE name LIKE ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    private Salle mapResultSet(ResultSet rs) throws SQLException {
        return new Salle(
                rs.getInt("id"),
                rs.getString("block"),
                rs.getInt("number"),
                rs.getString("name"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"),
                rs.getInt("etage"),
                rs.getInt("capacite")
        );
    }
}
