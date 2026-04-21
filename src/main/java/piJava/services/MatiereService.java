package piJava.services;

import piJava.entities.Matiere;
import piJava.entities.Classe;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatiereService implements ICrud<Matiere> {

    private Connection con;

    public MatiereService() {
        this.con = MyDataBase.getInstance().getConnection();
    }

    // ─── SHOW (SELECT ALL) ───────────────────────────────────────
    @Override
    public List<Matiere> show() throws SQLException {
        List<Matiere> matieres = new ArrayList<>();
        String query = "SELECT * FROM matiere_classe";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Matiere m = mapResultSet(rs);
                m.setClasses(getClassesForMatiere(m.getId()));
                matieres.add(m);
            }
        }
        return matieres;
    }

    // ─── ADD (INSERT) ────────────────────────────────────────────
    @Override
    public void add(Matiere matiere) throws SQLException {
        String query = "INSERT INTO matiere_classe (coefficient, chargehoraire, scorecomplexite, nom, description) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, matiere.getCoefficient());
            ps.setInt(2, matiere.getChargehoraire());
            ps.setInt(3, matiere.getScorecomplexite());
            ps.setString(4, matiere.getNom());
            ps.setString(5, matiere.getDescription());
            ps.executeUpdate();

            // Retrieve generated ID and set it back on the object
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    matiere.setId(generatedKeys.getInt(1));
                }
            }

            // Insert Many-to-Many links if any classes are attached
            if (matiere.getClasses() != null && !matiere.getClasses().isEmpty()) {
                insertClasseLinks(matiere.getId(), matiere.getClasses());
            }
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────
    @Override
    public void delete(int id) throws SQLException {
        // Remove join table entries first (FK constraint)
        String deleteLinks = "DELETE FROM matiere_classe_classe WHERE matiere_classe_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteLinks)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        String deleteMatiere = "DELETE FROM matiere_classe WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteMatiere)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── EDIT (UPDATE) ───────────────────────────────────────────
    @Override
    public void edit(Matiere matiere) throws SQLException {
        String query = "UPDATE matiere_classe SET coefficient = ?, chargehoraire = ?, scorecomplexite = ?, nom = ?, description = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setDouble(1, matiere.getCoefficient());
            ps.setInt(2, matiere.getChargehoraire());
            ps.setInt(3, matiere.getScorecomplexite());
            ps.setString(4, matiere.getNom());
            ps.setString(5, matiere.getDescription());
            ps.setInt(6, matiere.getId());
            ps.executeUpdate();
        }

        // Refresh Many-to-Many links: delete old ones, insert new ones
        String deleteLinks = "DELETE FROM matiere_classe_classe WHERE matiere_classe_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteLinks)) {
            ps.setInt(1, matiere.getId());
            ps.executeUpdate();
        }

        if (matiere.getClasses() != null && !matiere.getClasses().isEmpty()) {
            insertClasseLinks(matiere.getId(), matiere.getClasses());
        }
    }

    // ─── GET BY ID ───────────────────────────────────────────────
    public Matiere getById(int id) throws SQLException {
        String query = "SELECT * FROM matiere_classe WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Matiere m = mapResultSet(rs);
                    m.setClasses(getClassesForMatiere(m.getId()));
                    return m;
                }
            }
        }
        return null;
    }

    // ─── GET MATIERES BY CLASSE ──────────────────────────────────────────────
    public List<Matiere> getMatieresByClasseId(int classeId) throws SQLException {
        List<Matiere> matieres = new ArrayList<>();
        String query = """
                SELECT DISTINCT m.*
                FROM matiere_classe m
                JOIN matiere_classe_classe mcc ON m.id = mcc.matiere_classe_id
                WHERE mcc.classe_id = ?
                ORDER BY m.nom
                """;

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, classeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matieres.add(mapResultSet(rs));
                }
            }
        }
        return matieres;
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────

    // Maps a ResultSet row to a Matiere object
    private Matiere mapResultSet(ResultSet rs) throws SQLException {
        return new Matiere(
                rs.getInt("id"),
                rs.getDouble("coefficient"),
                rs.getInt("chargehoraire"),
                rs.getInt("scorecomplexite"),
                rs.getString("nom"),
                rs.getString("description")
        );
    }

    // Fetches all Classe records linked to a given matiereId
    private List<Classe> getClassesForMatiere(int matiereId) throws SQLException {
        List<Classe> classes = new ArrayList<>();
        String query = """
                SELECT c.* FROM classe c
                JOIN matiere_classe_classe mcc ON c.id = mcc.classe_id
                WHERE mcc.matiere_classe_id = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, matiereId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    classes.add(new Classe(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("niveau"),
                            rs.getString("anneeuniversitaire"),
                            rs.getString("description"),
                            rs.getString("filiere"),
                            rs.getObject("user_id") != null ? rs.getInt("user_id") : null
                    ));
                }
            }
        }
        return classes;
    }

    // Inserts rows into the join table matiere_classe_classe
    private void insertClasseLinks(int matiereId, List<Classe> classes) throws SQLException {
        String query = "INSERT INTO matiere_classe_classe (matiere_classe_id, classe_id) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            for (Classe c : classes) {
                ps.setInt(1, matiereId);
                ps.setInt(2, c.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}