package piJava.services;

import piJava.entities.suiviTache;
import piJava.services.ICrud;
import piJava.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public void edit(suiviTache suiviTache) throws SQLException {

    }

    public void add(suiviTache s) throws SQLException {
        String sql = "INSERT INTO suivi_tache(tache_id,date_action,ancien_statut,nouveau_statut,commentaire) VALUES (?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, s.getTache().getId());
        ps.setTimestamp(2, new java.sql.Timestamp(s.getDateAction().getTime()));
        ps.setString(3, s.getAncienStatut());
        ps.setString(4, s.getNouveauStatut());
        ps.setString(5, s.getCommentaire());
        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM suivi_tache WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<suiviTache> showByTask(int id) throws SQLException {
        List<suiviTache> st = new ArrayList<>();

        String sql = "SELECT * FROM suivi_tache WHERE tache_id =? ORDER BY date_action DESC";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            suiviTache s = new suiviTache();
            s.setId(rs.getInt("id"));
            s.setDateAction(rs.getTimestamp("date_action"));
            s.setAncienStatut(rs.getString("ancien_statut"));
            s.setNouveauStatut(rs.getString("nouveau_Statut"));
            s.setCommentaire(rs.getString("commentaire"));
            st.add(s);
        }
        return st;
    }


}
