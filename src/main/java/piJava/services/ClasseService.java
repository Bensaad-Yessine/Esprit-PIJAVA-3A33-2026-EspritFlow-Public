package piJava.services;

import piJava.entities.Classe;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasseService implements ICrud<Classe> {

    private final Connection con = MyDataBase.getInstance().getConnection();

    // ─── SHOW (SELECT ALL) ───────────────────────────────────────
    @Override
    public List<Classe> show() throws SQLException {
        return getAllClasses();
    }

    public List<Classe> getAllClasses() {
        List<Classe> list = new ArrayList<>();
        String sql = "SELECT * FROM classe";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAllClasses: " + e.getMessage());
        }
        return list;
    }

    // ─── ADD (INSERT) ────────────────────────────────────────────
    @Override
    public void add(Classe classe) throws SQLException {
        String sql = "INSERT INTO classe (nom, niveau, anneeuniversitaire, description, filiere, user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, classe.getNom());
            ps.setString(2, classe.getNiveau());
            ps.setString(3, classe.getAnneeUniversitaire());
            ps.setString(4, classe.getDescription());
            ps.setString(5, classe.getFiliere());

            if (classe.getUserId() != null) {
                ps.setInt(6, classe.getUserId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    classe.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────
    @Override
    public void delete(int id) throws SQLException {
        // Remove join table entries first (FK constraint)
        String deleteLinks = "DELETE FROM matiere_classe_classe WHERE classe_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteLinks)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        String sql = "DELETE FROM classe WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── EDIT (UPDATE) ───────────────────────────────────────────
    @Override
    public void edit(Classe classe) throws SQLException {
        String sql = "UPDATE classe SET nom = ?, niveau = ?, anneeuniversitaire = ?, description = ?, filiere = ?, user_id = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, classe.getNom());
            ps.setString(2, classe.getNiveau());
            ps.setString(3, classe.getAnneeUniversitaire());
            ps.setString(4, classe.getDescription());
            ps.setString(5, classe.getFiliere());

            if (classe.getUserId() != null) {
                ps.setInt(6, classe.getUserId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, classe.getId());
            ps.executeUpdate();
        }
    }

    // ─── GET BY ID ───────────────────────────────────────────────
    public Classe getById(int id) throws SQLException {
        String sql = "SELECT * FROM classe WHERE id = ?";

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
    public List<Classe> searchByNom(String keyword) throws SQLException {
        List<Classe> list = new ArrayList<>();
        String sql = "SELECT * FROM classe WHERE nom LIKE ?";

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

    // ─── GET BY NIVEAU ───────────────────────────────────────────
    public List<Classe> getByNiveau(String niveau) throws SQLException {
        List<Classe> list = new ArrayList<>();
        String sql = "SELECT * FROM classe WHERE niveau = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, niveau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    // ─── GET BY FILIERE ──────────────────────────────────────────
    public List<Classe> getByFiliere(String filiere) throws SQLException {
        List<Classe> list = new ArrayList<>();
        String sql = "SELECT * FROM classe WHERE filiere = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filiere);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    // ─── PRIVATE HELPER ──────────────────────────────────────────
    private Classe mapResultSet(ResultSet rs) throws SQLException {
        return new Classe(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("niveau"),
                rs.getString("anneeuniversitaire"),
                rs.getString("description"),
                rs.getString("filiere"),
                rs.getObject("user_id", Integer.class)
        );
    }
}