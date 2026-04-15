package piJava.services;

import piJava.entities.tache;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TacheService implements ICrud<tache> {

    Connection con;

    public TacheService() {
        con = MyDataBase.getInstance().getConnection();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void add(tache t) throws SQLException {
        Connection connection = requireConnection();
        String sql = "INSERT INTO tache (titre, type, date_debut, date_fin, priorite, statut, user_id, created_at, date_echeance, description, duree_estimee, updated_at, prediction) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            t.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection connection = requireConnection();
        // 1. Delete related suivi_tache first (foreign key constraint)
        String sqlChild = "DELETE FROM suivi_tache WHERE tache_id = ?";
        PreparedStatement psChild = connection.prepareStatement(sqlChild);
        psChild.setInt(1, id);
        psChild.executeUpdate();
        psChild.close();

        // 2. Delete the tache
        String sqlParent = "DELETE FROM tache WHERE id = ?";
        PreparedStatement psParent = connection.prepareStatement(sqlParent);
        psParent.setInt(1, id);
        psParent.executeUpdate();
        psParent.close();
    }

    @Override
    public List<tache> show() throws SQLException {
        Connection connection = requireConnection();
        List<tache> taches = new ArrayList<>();
        String sql = "SELECT * FROM tache";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            taches.add(mapRow(rs));
        }
        return taches;
    }

    /**
     * ICrud requires edit(T t) — the tache must have its id set before calling.
     * Use t.setId(id) before passing to this method.
     */
    @Override
    public void edit(tache t) throws SQLException {
        Connection connection = requireConnection();
        String sql = "UPDATE tache SET titre=?, type=?, date_debut=?, date_fin=?, priorite=?, statut=?, user_id=?, date_echeance=?, description=?, duree_estimee=?, updated_at=?, prediction=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1,  t.getTitre()       != null ? t.getTitre()       : null);
        ps.setString(2,  t.getType()        != null ? t.getType()        : null);
        ps.setTimestamp(3,  t.getDate_debut()   != null ? new Timestamp(t.getDate_debut().getTime())   : null);
        ps.setTimestamp(4,  t.getDate_fin()     != null ? new Timestamp(t.getDate_fin().getTime())     : null);
        ps.setString(5,  t.getPriorite()    != null ? t.getPriorite()    : null);
        ps.setString(6,  t.getStatut()      != null ? t.getStatut()      : null);
        ps.setInt(7,     t.getUser_id());
        ps.setTimestamp(8,  t.getDate_echeance() != null ? new Timestamp(t.getDate_echeance().getTime()) : null);
        ps.setString(9,  t.getDescription() != null ? t.getDescription() : null);
        ps.setInt(10,    t.getDuree_estimee());
        ps.setTimestamp(11, new Timestamp(System.currentTimeMillis())); // updated_at = now
        ps.setDouble(12, t.getPrediction());
        ps.setInt(13,    t.getId());   // ← WHERE id comes from the tache object itself

        ps.executeUpdate();
        ps.close();
    }

    // ── Frontend-specific ──────────────────────────────────────────────────────

    public List<tache> showUserTasks(int userId) throws SQLException {
        Connection connection = requireConnection();
        List<tache> taches = new ArrayList<>();
        String sql = "SELECT * FROM tache WHERE user_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            taches.add(mapRow(rs));
        }
        ps.close();
        return taches;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private tache mapRow(ResultSet rs) throws SQLException {
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
        t.setDescription(rs.getString("description") != null ? rs.getString("description") : "pas de description");
        t.setDuree_estimee(rs.getInt("duree_estimee"));
        t.setPrediction(rs.getDouble("prediction"));
        return t;
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable. Verify MySQL is running and the 'pidev' database exists.");
        }
        return con;
    }
}
