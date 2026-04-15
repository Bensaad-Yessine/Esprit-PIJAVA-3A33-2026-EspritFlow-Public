package piJava.services;

import piJava.entities.preferenceAlerte;
import piJava.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlerteService implements ICrud<preferenceAlerte> {

    Connection con;

    public AlerteService() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public List<preferenceAlerte> show() throws SQLException {
        return new ArrayList<>(); // implement if needed for admin view
    }

    @Override
    public void add(preferenceAlerte p) throws SQLException {
        Connection connection = requireConnection();
        String sql = "INSERT INTO preference_alerte (nom, is_active, is_default, email_actif, push_actif, site_notif_active, delai_rappel_min, heure_silence_debut, heure_silence_fin, date_creation, date_mise_ajour, etudiant_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getNom());
        ps.setBoolean(2, p.getIs_active());
        ps.setBoolean(3, p.getIs_default());
        ps.setBoolean(4, p.getEmail_actif());
        ps.setBoolean(5, p.getPush_actif());
        ps.setBoolean(6, p.getSite_notif_active());
        ps.setInt(7, p.getDelai_rappel_min());
        ps.setTime(8, java.sql.Time.valueOf(p.getHeure_silence_debut()));
        ps.setTime(9, java.sql.Time.valueOf(p.getHeure_silence_fin()));
        ps.setTimestamp(10, new java.sql.Timestamp(p.getDate_creation().getTime()));
        ps.setTimestamp(11, new java.sql.Timestamp(p.getDate_mise_ajour().getTime()));
        ps.setInt(12, p.getUser_id());
        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection connection = requireConnection();
        String sql = "DELETE FROM preference_alerte WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    /** ✅ edit(T t) — id comes from p.getId() */
    @Override
    public void edit(preferenceAlerte p) throws SQLException {
        Connection connection = requireConnection();
        String sql = "UPDATE preference_alerte SET nom=?, is_active=?, is_default=?, email_actif=?, push_actif=?, site_notif_active=?, delai_rappel_min=?, heure_silence_debut=?, heure_silence_fin=?, date_mise_ajour=?, etudiant_id=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getNom());
        ps.setBoolean(2, p.getIs_active());
        ps.setBoolean(3, p.getIs_default());
        ps.setBoolean(4, p.getEmail_actif());
        ps.setBoolean(5, p.getPush_actif());
        ps.setBoolean(6, p.getSite_notif_active());
        ps.setInt(7, p.getDelai_rappel_min());
        ps.setTime(8, java.sql.Time.valueOf(p.getHeure_silence_debut()));
        ps.setTime(9, java.sql.Time.valueOf(p.getHeure_silence_fin()));
        ps.setTimestamp(10, new java.sql.Timestamp(System.currentTimeMillis()));
        ps.setInt(11, p.getUser_id());
        ps.setInt(12, p.getId()); // ✅ WHERE id from object
        ps.executeUpdate();
        ps.close();
    }

    public List<preferenceAlerte> showUserAlertes(int userId) throws SQLException {
        Connection connection = requireConnection();
        List<preferenceAlerte> alertes = new ArrayList<>();
        String sql = "SELECT * FROM preference_alerte WHERE etudiant_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            preferenceAlerte p = new preferenceAlerte();
            p.setId(rs.getInt("id"));
            p.setUser_id(rs.getInt("etudiant_id"));
            p.setNom(rs.getString("nom"));
            p.setIs_active(rs.getBoolean("is_active"));
            p.setIs_default(rs.getBoolean("is_default"));
            p.setEmail_actif(rs.getBoolean("email_actif"));
            p.setPush_actif(rs.getBoolean("push_actif"));
            p.setSite_notif_active(rs.getBoolean("site_notif_active"));

            int delai = rs.getInt("delai_rappel_min");
            if (!rs.wasNull()) p.setDelai_rappel_min(delai);

            java.sql.Time heureDebut = rs.getTime("heure_silence_debut");
            if (heureDebut != null) p.setHeure_silence_debut(heureDebut.toLocalTime());

            java.sql.Time heureFin = rs.getTime("heure_silence_fin");
            if (heureFin != null) p.setHeure_silence_fin(heureFin.toLocalTime());

            java.sql.Timestamp dateCreation = rs.getTimestamp("date_creation");
            if (dateCreation != null) p.setDate_creation(dateCreation);

            java.sql.Timestamp dateMaj = rs.getTimestamp("date_mise_ajour");
            if (dateMaj != null) p.setDate_mise_ajour(dateMaj);

            alertes.add(p);
        }
        ps.close();
        return alertes;
    }

    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable. Verify MySQL is running and the 'pidev' database exists.");
        }
        return con;
    }
}
