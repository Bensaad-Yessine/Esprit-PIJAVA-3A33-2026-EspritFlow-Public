package piJava.services;

import piJava.entities.preferenceAlerte;
import piJava.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;


public class AlerteService implements ICrud<preferenceAlerte> {

    Connection con;
    public AlerteService() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public List<preferenceAlerte> show() throws SQLException {
        List<preferenceAlerte> alertes = new ArrayList<preferenceAlerte>();
        String sql = "select * from preference_alerte ";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            preferenceAlerte p = new preferenceAlerte();
            p.setId(rs.getInt("id"));
            p.setUser_id(rs.getInt("etudiant_id"));
            p.setNom(rs.getString("nom"));
            p.setIs_active(rs.getBoolean("is_active"));
            p.setIs_default(rs.getBoolean("is_default"));
            p.setEmail_actif(rs.getBoolean("email_actif"));
            p.setPush_actif(rs.getBoolean("push_actif"));
            p.setSite_notif_active(rs.getBoolean("site_notif_active"));

            // Handle nullable Integer
            int delai = rs.getInt("delai_rappel_min");
            if (!rs.wasNull()) {
                p.setDelai_rappel_min(delai);
            }

            // Handle nullable Time fields
            java.sql.Time heureDebut = rs.getTime("heure_silence_debut");
            if (heureDebut != null) {
                p.setHeure_silence_debut(heureDebut.toLocalTime());
            }

            java.sql.Time heureFin = rs.getTime("heure_silence_fin");
            if (heureFin != null) {
                p.setHeure_silence_fin(heureFin.toLocalTime());
            }

            // Handle nullable Timestamp fields
            java.sql.Timestamp dateCreation = rs.getTimestamp("date_creation");
            if (dateCreation != null) {
                p.setDate_creation(dateCreation);
            }

            java.sql.Timestamp dateMaj = rs.getTimestamp("date_mise_ajour");
            if (dateMaj != null) {
                p.setDate_mise_ajour(dateMaj);
            }

            alertes.add(p);
        }
        return alertes;
    }

    @Override
    public void edit(preferenceAlerte preferenceAlerte) throws SQLException {
        String sql = "UPDATE preference_alerte SET nom=?, is_active=?, is_default=?, email_actif=?, push_actif=?, site_notif_active=?, delai_rappel_min=?, heure_silence_debut=?, heure_silence_fin=?, date_mise_ajour=?, etudiant_id=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, preferenceAlerte.getNom());
        ps.setBoolean(2, preferenceAlerte.getIs_active());
        ps.setBoolean(3, preferenceAlerte.getIs_default());
        ps.setBoolean(4, preferenceAlerte.getEmail_actif());
        ps.setBoolean(5, preferenceAlerte.getPush_actif());
        ps.setBoolean(6, preferenceAlerte.getSite_notif_active());
        ps.setInt(7, preferenceAlerte.getDelai_rappel_min());
        ps.setTime(8, java.sql.Time.valueOf(preferenceAlerte.getHeure_silence_debut()));
        ps.setTime(9, java.sql.Time.valueOf(preferenceAlerte.getHeure_silence_fin()));
        ps.setTimestamp(10, new java.sql.Timestamp(preferenceAlerte.getDate_mise_ajour().getTime()));
        ps.setInt(11, preferenceAlerte.getUser_id());
        ps.setInt(12, preferenceAlerte.getId());
        ps.executeUpdate();
    }

    @Override
    public void add(preferenceAlerte preferenceAlerte) throws SQLException {
        String sql = "INSERT INTO preference_alerte (`nom`, `is_active`, `is_default`, `email_actif`, `push_actif`, `site_notif_active`, `delai_rappel_min`, `heure_silence_debut`, `heure_silence_fin`, `date_creation`, `date_mise_ajour`, `etudiant_id`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, preferenceAlerte.getNom());
        ps.setBoolean(2, preferenceAlerte.getIs_active());
        ps.setBoolean(3, preferenceAlerte.getIs_default());
        ps.setBoolean(4, preferenceAlerte.getEmail_actif());
        ps.setBoolean(5, preferenceAlerte.getPush_actif());
        ps.setBoolean(6, preferenceAlerte.getSite_notif_active());
        ps.setInt(7, preferenceAlerte.getDelai_rappel_min());
        ps.setTime(8, java.sql.Time.valueOf(preferenceAlerte.getHeure_silence_debut()));
        ps.setTime(9, java.sql.Time.valueOf(preferenceAlerte.getHeure_silence_fin()));
        ps.setTimestamp(10, new java.sql.Timestamp(preferenceAlerte.getDate_creation().getTime()));
        ps.setTimestamp(11, new java.sql.Timestamp(preferenceAlerte.getDate_mise_ajour().getTime()));
        ps.setInt(12, preferenceAlerte.getUser_id());
        ps.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM preference_alerte WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Preference Alerte with ID " + id + " has been deleted.");
    }


    public List<preferenceAlerte> showUserAlertes(int id) throws SQLException {
        List<preferenceAlerte> alertes = new ArrayList<preferenceAlerte>();
        String sql = "select * from preference_alerte where etudiant_id=?"; // ⚠️ Changed from id to user_id
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            preferenceAlerte p = new preferenceAlerte();
            p.setId(rs.getInt("id"));
            p.setUser_id(rs.getInt("etudiant_id"));
            p.setNom(rs.getString("nom"));
            p.setIs_active(rs.getBoolean("is_active"));
            p.setIs_default(rs.getBoolean("is_default"));
            p.setEmail_actif(rs.getBoolean("email_actif"));
            p.setPush_actif(rs.getBoolean("push_actif"));
            p.setSite_notif_active(rs.getBoolean("site_notif_active"));

            // Handle nullable Integer
            int delai = rs.getInt("delai_rappel_min");
            if (!rs.wasNull()) {
                p.setDelai_rappel_min(delai);
            }

            // Handle nullable Time fields
            java.sql.Time heureDebut = rs.getTime("heure_silence_debut");
            if (heureDebut != null) {
                p.setHeure_silence_debut(heureDebut.toLocalTime());
            }

            java.sql.Time heureFin = rs.getTime("heure_silence_fin");
            if (heureFin != null) {
                p.setHeure_silence_fin(heureFin.toLocalTime());
            }

            // Handle nullable Timestamp fields
            java.sql.Timestamp dateCreation = rs.getTimestamp("date_creation");
            if (dateCreation != null) {
                p.setDate_creation(dateCreation);
            }

            java.sql.Timestamp dateMaj = rs.getTimestamp("date_mise_ajour");
            if (dateMaj != null) {
                p.setDate_mise_ajour(dateMaj);
            }

            alertes.add(p);
        }
        return alertes;
    }

    public void setActive(int selectedId, int userId) throws SQLException {
        String sql = "UPDATE preference_alerte SET is_active = CASE WHEN id = ? THEN 1 ELSE 0 END WHERE etudiant_id = ?";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, selectedId);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }
}
