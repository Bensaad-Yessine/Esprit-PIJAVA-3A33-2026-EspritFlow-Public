package piJava.services;

import piJava.entities.Seance;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeanceService implements ICrud<Seance> {

    private Connection con;

    public SeanceService() {
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
    public List<Seance> show() throws SQLException {
        return getAllSeances();
    }

    public List<Seance> getAllSeances() throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = "SELECT * FROM seance";
        try (Statement st = requireConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public void add(Seance seance) throws SQLException {
        String sql = "INSERT INTO seance (jour, type_seance, mode, heure_debut, heure_fin, created_at, salle_id, matiere_id, classe_id, qr_token, qr_expires_at, qr_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, seance.getJour());
            ps.setString(2, seance.getTypeSeance());
            ps.setString(3, seance.getMode());
            ps.setTimestamp(4, seance.getHeureDebut());
            ps.setTimestamp(5, seance.getHeureFin());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.setInt(7, seance.getSalleId());
            ps.setInt(8, seance.getMatiereId());
            ps.setInt(9, seance.getClasseId());
            ps.setString(10, seance.getQrToken());
            ps.setTimestamp(11, seance.getQrExpiresAt());
            ps.setString(12, seance.getQrUrl());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    seance.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void edit(Seance seance) throws SQLException {
        String sql = "UPDATE seance SET jour = ?, type_seance = ?, mode = ?, heure_debut = ?, heure_fin = ?, salle_id = ?, matiere_id = ?, classe_id = ?, qr_token = ?, qr_expires_at = ?, qr_url = ? WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setString(1, seance.getJour());
            ps.setString(2, seance.getTypeSeance());
            ps.setString(3, seance.getMode());
            ps.setTimestamp(4, seance.getHeureDebut());
            ps.setTimestamp(5, seance.getHeureFin());
            ps.setInt(6, seance.getSalleId());
            ps.setInt(7, seance.getMatiereId());
            ps.setInt(8, seance.getClasseId());
            ps.setString(9, seance.getQrToken());
            ps.setTimestamp(10, seance.getQrExpiresAt());
            ps.setString(11, seance.getQrUrl());
            ps.setInt(12, seance.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM seance WHERE id = ?";
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Seance getById(int id) throws SQLException {
        String sql = "SELECT * FROM seance WHERE id = ?";
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

    private Seance mapResultSet(ResultSet rs) throws SQLException {
        return new Seance(
                rs.getInt("id"),
                rs.getString("jour"),
                rs.getString("type_seance"),
                rs.getString("mode"),
                rs.getTimestamp("heure_debut"),
                rs.getTimestamp("heure_fin"),
                rs.getTimestamp("created_at"),
                rs.getInt("salle_id"),
                rs.getInt("matiere_id"),
                rs.getInt("classe_id"),
                rs.getString("qr_token"),
                rs.getTimestamp("qr_expires_at"),
                rs.getString("qr_url")
        );
    }
}
