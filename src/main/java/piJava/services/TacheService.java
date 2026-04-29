package piJava.services;

import piJava.entities.tache;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

public class TacheService implements ICrud<tache> {


    Connection con;
    public TacheService() {
        con = MyDataBase.getInstance().getConnection();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void add(tache t) throws SQLException {
        String sql = "INSERT INTO tache (titre, type, date_debut, date_fin, priorite, statut, user_id, created_at, date_echeance, description, duree_estimee, updated_at, prediction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // ⚡ Request generated keys
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, t.getTitre());
        ps.setString(2, t.getType());
        ps.setTimestamp(3, new Timestamp(t.getDate_debut().getTime()));
        ps.setTimestamp(4, new Timestamp(t.getDate_fin().getTime()));
        ps.setString(5, t.getPriorite());
        ps.setString(6, t.getStatut());
        ps.setInt(7, t.getUser_id());
        ps.setTimestamp(8, t.getCreated_at() != null ? new Timestamp(t.getCreated_at().getTime()) : null);
        ps.setTimestamp(9, t.getDate_echeance() != null ? new Timestamp(t.getDate_echeance().getTime()) : null);
        ps.setString(10, t.getDescription());
        ps.setInt(11, t.getDuree_estimee());
        ps.setTimestamp(12, t.getUpdated_at() != null ? new Timestamp(t.getUpdated_at().getTime()) : null);
        ps.setDouble(13, t.getPrediction());

        // ⚡ Execute first
        ps.executeUpdate();

        // ⚡ Then get generated key
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            t.setId(keys.getInt(1)); // now t.getId() has the DB-assigned ID
        }
        keys.close();
        ps.close();
    }

    @Override
    public void delete(int id) throws SQLException {
        // 1. Delete related suivi_tache
        String sqlChild = "DELETE FROM suivi_tache WHERE tache_id = ?";
        PreparedStatement psChild = con.prepareStatement(sqlChild);
        psChild.setInt(1, id);
        int childRows = psChild.executeUpdate();
        System.out.println("Deleted " + childRows + " child history rows.");

        // 2. Delete the tache
        String sqlParent = "DELETE FROM tache WHERE id = ?";
        PreparedStatement psParent = con.prepareStatement(sqlParent);
        psParent.setInt(1, id);
        int parentRows = psParent.executeUpdate();
        System.out.println("Deleted " + parentRows + " task row.");
    }

    @Override
    public List<tache> show() throws SQLException {
        List<tache> taches = new ArrayList<>();

        String sql = "SELECT * FROM tache";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            tache t = new tache();
            t.setId(rs.getInt("id"));
            t.setTitre(rs.getString("titre"));
            t.setType(rs.getString("type"));
            t.setDate_debut(rs.getTimestamp("date_debut"));
            t.setDate_fin(rs.getTimestamp("date_fin"));
            t.setPriorite(rs.getString("priorite"));
            t.setStatut(rs.getString("statut"));
            t.setUser_id(rs.getInt("user_id"));
            t.setDate_echeance(rs.getTimestamp("date_echeance"));
            if (rs.getString("description") != null) {
                t.setDescription(rs.getString("description"));
            } else  t.setDescription("pas de description");
            t.setDuree_estimee(rs.getInt("duree_estimee"));
            t.setPrediction(rs.getDouble("prediction"));
            t.setCreated_at(rs.getTimestamp("created_at"));
            t.setUpdated_at(rs.getTimestamp("updated_at"));
            taches.add(t);
        }

        return taches;
    }

    @Override
    public void edit(tache t) throws SQLException {
        // 2️⃣ Update the task
        String sqlUpdate = "UPDATE tache SET titre=?, type=?, date_debut=?, date_fin=?, priorite=?, statut=?, user_id=?, date_echeance=?, description=?, duree_estimee=?, updated_at=?, prediction=? WHERE id=?";
        PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);

        // Strings
        if (t.getTitre() != null) psUpdate.setString(1, t.getTitre());
        else psUpdate.setNull(1, java.sql.Types.VARCHAR);

        if (t.getType() != null) psUpdate.setString(2, t.getType());
        else psUpdate.setNull(2, java.sql.Types.VARCHAR);

        // Dates / DATETIME
        if (t.getDate_debut() != null) psUpdate.setTimestamp(3, new java.sql.Timestamp(t.getDate_debut().getTime()));
        else psUpdate.setNull(3, java.sql.Types.TIMESTAMP);

        if (t.getDate_fin() != null) psUpdate.setTimestamp(4, new java.sql.Timestamp(t.getDate_fin().getTime()));
        else psUpdate.setNull(4, java.sql.Types.TIMESTAMP);

        if (t.getPriorite() != null) psUpdate.setString(5, t.getPriorite());
        else psUpdate.setNull(5, java.sql.Types.VARCHAR);

        if (t.getStatut() != null) psUpdate.setString(6, t.getStatut());
        else psUpdate.setNull(6, java.sql.Types.VARCHAR);

        // Numbers
        psUpdate.setObject(7, t.getUser_id(), java.sql.Types.INTEGER);

        if (t.getDate_echeance() != null) psUpdate.setTimestamp(8, new java.sql.Timestamp(t.getDate_echeance().getTime()));
        else psUpdate.setNull(8, java.sql.Types.TIMESTAMP);

        if (t.getDescription() != null) psUpdate.setString(9, t.getDescription());
        else psUpdate.setNull(9, java.sql.Types.VARCHAR);

        psUpdate.setObject(10, t.getDuree_estimee(), java.sql.Types.DOUBLE);

        // Updated at = current timestamp
        psUpdate.setTimestamp(11, new java.sql.Timestamp(System.currentTimeMillis()));

        psUpdate.setObject(12, t.getPrediction(), java.sql.Types.DOUBLE);

        // WHERE id
        psUpdate.setInt(13, t.getId());

        int rowsUpdated = psUpdate.executeUpdate();
        System.out.println("Tache modifiée avec succès. Lignes affectées: " + rowsUpdated);

        psUpdate.close();
    }


    // ========== FRONTEND-SPECIFIC METHODS ==========

    public List<tache> showUserTasks(int id) throws SQLException {
        List<tache> taches = new ArrayList<>();


        String sql = "SELECT * FROM tache WHERE user_id = ? ";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1,id);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            tache t = new tache();
            t.setId(rs.getInt("id"));
            t.setTitre(rs.getString("titre"));
            t.setType(rs.getString("type"));
            t.setDate_debut(rs.getTimestamp("date_debut"));
            t.setDate_fin(rs.getTimestamp("date_fin"));
            t.setPriorite(rs.getString("priorite"));
            t.setStatut(rs.getString("statut"));
            t.setUser_id(rs.getInt("user_id"));
            t.setDate_echeance(rs.getTimestamp("date_echeance"));
            if (rs.getString("description") != null) {
                t.setDescription(rs.getString("description"));
            } else  t.setDescription("pas de description");
            t.setDuree_estimee(rs.getInt("duree_estimee"));
            t.setPrediction(rs.getDouble("prediction"));
            t.setCreated_at(rs.getTimestamp("created_at"));
            t.setUpdated_at(rs.getTimestamp("updated_at"));
            taches.add(t);
        }

        return taches;
    }


    public List<tache> getTasksByUserSince(int userId, LocalDateTime since) {
        List<tache> taches = new ArrayList<>();
        String sql = "SELECT * FROM tache WHERE user_id = ? AND created_at >= ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(since));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tache t = new tache();
                t.setId(rs.getInt("id"));
                t.setTitre(rs.getString("titre"));
                t.setType(rs.getString("type"));
                t.setDate_debut(rs.getTimestamp("date_debut"));
                t.setDate_fin(rs.getTimestamp("date_fin"));
                t.setPriorite(rs.getString("priorite"));
                t.setStatut(rs.getString("statut"));
                t.setUser_id(rs.getInt("user_id"));
                t.setDate_echeance(rs.getTimestamp("date_echeance"));
                t.setDescription(rs.getString("description"));
                t.setDuree_estimee(rs.getInt("duree_estimee"));
                t.setPrediction(rs.getDouble("prediction"));
                t.setCreated_at(rs.getTimestamp("created_at"));
                t.setUpdated_at(rs.getTimestamp("updated_at"));
                taches.add(t);
            }
        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }

        return taches;
    }


    public tache showById(int tacheId) {
        String sql = "SELECT * FROM tache WHERE id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, tacheId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tache t = new tache();
                t.setId(rs.getInt("id"));
                t.setTitre(rs.getString("titre"));
                t.setType(rs.getString("type"));
                t.setDate_debut(rs.getTimestamp("date_debut"));
                t.setDate_fin(rs.getTimestamp("date_fin"));
                t.setPriorite(rs.getString("priorite"));
                t.setStatut(rs.getString("statut"));
                t.setUser_id(rs.getInt("user_id"));
                t.setDate_echeance(rs.getTimestamp("date_echeance"));
                if (rs.getString("description") != null) {
                    t.setDescription(rs.getString("description"));
                } else t.setDescription("pas de description");
                t.setDuree_estimee(rs.getInt("duree_estimee"));
                t.setPrediction(rs.getDouble("prediction"));
                t.setCreated_at(rs.getTimestamp("created_at"));
                t.setUpdated_at(rs.getTimestamp("updated_at"));
                return t;
            } return null;

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
            return null;
        }
    }

    public tache getById(Integer tacheId) {
        String sql = "SELECT * FROM tache WHERE id = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, tacheId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tache t = new tache();
                t.setId(rs.getInt("id"));
                t.setTitre(rs.getString("titre"));
                t.setType(rs.getString("type"));
                t.setDate_debut(rs.getTimestamp("date_debut"));
                t.setDate_fin(rs.getTimestamp("date_fin"));
                t.setPriorite(rs.getString("priorite"));
                t.setStatut(rs.getString("statut"));
                t.setUser_id(rs.getInt("user_id"));
                t.setDate_echeance(rs.getTimestamp("date_echeance"));
                if (rs.getString("description") != null) {
                    t.setDescription(rs.getString("description"));
                } else  t.setDescription("pas de description");
                t.setDuree_estimee(rs.getInt("duree_estimee"));
                t.setPrediction(rs.getDouble("prediction"));
                t.setCreated_at(rs.getTimestamp("created_at"));
                t.setUpdated_at(rs.getTimestamp("updated_at"));
                return t;
            } return null;

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
            return null;
        }

    }
}

