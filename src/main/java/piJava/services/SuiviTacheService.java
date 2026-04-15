package piJava.services;

import piJava.entities.suiviTache;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuiviTacheService implements ICrud<suiviTache> {

    Connection con;

    public SuiviTacheService() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public List<suiviTache> show() throws SQLException {
        return List.of();
    }

    @Override
    public void add(suiviTache s) throws SQLException {
        Connection connection = requireConnection();
        String sql = "INSERT INTO suivi_tache (tache_id, date_action, ancien_statut, nouveau_statut, commentaire) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, s.getTache().getId());
        ps.setTimestamp(2, new Timestamp(s.getDateAction().getTime()));
        ps.setString(3, s.getAncienStatut());
        ps.setString(4, s.getNouveauStatut());
        ps.setString(5, s.getCommentaire());
        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection connection = requireConnection();
        String sql = "DELETE FROM suivi_tache WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    /** ✅ Required by ICrud — id comes from s.getId() */
    @Override
    public void edit(suiviTache s) throws SQLException {
        Connection connection = requireConnection();
        String sql = "UPDATE suivi_tache SET date_action=?, ancien_statut=?, nouveau_statut=?, commentaire=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setTimestamp(1, new Timestamp(s.getDateAction().getTime()));
        ps.setString(2, s.getAncienStatut());
        ps.setString(3, s.getNouveauStatut());
        ps.setString(4, s.getCommentaire());
        ps.setInt(5, s.getId());
        ps.executeUpdate();
        ps.close();
    }

    public List<suiviTache> showByTask(int taskId) throws SQLException {
        Connection connection = requireConnection();
        List<suiviTache> list = new ArrayList<>();
        String sql = "SELECT * FROM suivi_tache WHERE tache_id = ? ORDER BY date_action DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, taskId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            suiviTache s = new suiviTache();
            s.setId(rs.getInt("id"));
            s.setDateAction(rs.getTimestamp("date_action"));
            s.setAncienStatut(rs.getString("ancien_statut"));
            s.setNouveauStatut(rs.getString("nouveau_statut"));
            s.setCommentaire(rs.getString("commentaire"));
            list.add(s);
        }
        ps.close();
        return list;
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable. Verify MySQL is running and the 'pidev' database exists.");
        }
        return con;
    }
}
