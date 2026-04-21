package piJava.services;

import piJava.entities.Groupe;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupeService implements ICrud<Groupe> {

    private final Connection con = MyDataBase.getInstance().getConnection();

    // ─── SHOW (SELECT ALL) ───────────────────────────────────────
    @Override
    public List<Groupe> show() throws SQLException {
        return getAll();
    }

    public List<Groupe> getAll() throws SQLException {
        List<Groupe> list = new ArrayList<>();
        String sql = "SELECT * FROM groupe_projet";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[GroupeService] Error getting all groups: " + e.getMessage());
            // Return empty list instead of throwing to allow UI to load
            return list;
        }
        return list;
    }

    // ─── ADD (INSERT) ────────────────────────────────────────────
    @Override
    public void add(Groupe groupe) throws SQLException {
        String sql = "INSERT INTO groupe_projet (nom_projet, matiere, nbr_membre, statut, description) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, groupe.getNom());
            ps.setString(2, groupe.getProjet());
            ps.setInt(3, groupe.getNbreMembre());
            ps.setString(4, groupe.getStatut());
            ps.setString(5, groupe.getDescription());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    groupe.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM groupe_projet WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── EDIT (UPDATE) ───────────────────────────────────────────
    @Override
    public void edit(Groupe groupe) throws SQLException {
        String sql = "UPDATE groupe_projet SET nom_projet = ?, matiere = ?, nbr_membre = ?, statut = ?, description = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, groupe.getNom());
            ps.setString(2, groupe.getProjet());
            ps.setInt(3, groupe.getNbreMembre());
            ps.setString(4, groupe.getStatut());
            ps.setString(5, groupe.getDescription());
            ps.setInt(6, groupe.getId());
            ps.executeUpdate();
        }
    }

    // ─── GET BY ID ───────────────────────────────────────────────
    public Groupe getById(int id) throws SQLException {
        String sql = "SELECT * FROM groupe_projet WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // ─── SEARCH BY NOM ───────────────────────────────────────────
    public List<Groupe> searchByNom(String keyword) throws SQLException {
        List<Groupe> list = new ArrayList<>();
        String sql = "SELECT * FROM groupe_projet WHERE nom_projet LIKE ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    // ─── GET BY STATUT ───────────────────────────────────────────
    public List<Groupe> getByStatut(String statut) throws SQLException {
        List<Groupe> list = new ArrayList<>();
        String sql = "SELECT * FROM groupe_projet WHERE statut = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    // ─── GET BY USER ID ──────────────────────────────────────────
    public List<Groupe> getByUserId(int userId) throws SQLException {
        List<Groupe> list = new ArrayList<>();
        String sql = "SELECT g.* FROM groupe_projet g " +
                     "INNER JOIN groupe_projet_user ug ON g.id = ug.groupe_projet_id " +
                     "WHERE ug.user_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    // ─── ADD USER TO GROUP ───────────────────────────────────────
    public void addUserToGroup(int groupeId, int userId) throws SQLException {
        String sql = "INSERT INTO groupe_projet_user (groupe_projet_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupeId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // ─── Result Set Mapper ────────────────────────────────────────
    private Groupe mapResultSet(ResultSet rs) throws SQLException {
        return new Groupe(
                rs.getInt("id"),
                rs.getString("nom_projet"),
                rs.getString("matiere"),
                rs.getInt("nbr_membre"),
                rs.getString("statut"),
                rs.getString("description")
        );
    }
}
