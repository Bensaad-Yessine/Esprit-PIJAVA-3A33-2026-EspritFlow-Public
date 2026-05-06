package piJava.services;

import piJava.entities.SuiviBienEtre;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuiviBienEtreService {

    private Connection cnx;

    public SuiviBienEtreService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    public void ajouter(SuiviBienEtre s) throws SQLException {
        String sql = "INSERT INTO suivi_bien_etre (date_saisie, humeur, qualite_sommeil, niveau_energie, niveau_stress, qualite_alimentation, notes_libres, score, objectif_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, s.getDateSaisie());
        ps.setString(2, s.getHumeur());
        ps.setInt(3, s.getQualiteSommeil());
        ps.setInt(4, s.getNiveauEnergie());
        ps.setInt(5, s.getNiveauStress());
        ps.setInt(6, s.getQualiteAlimentation());
        ps.setString(7, s.getNotesLibres());
        ps.setDouble(8, s.getScore());
        ps.setInt(9, s.getObjectifId());

        ps.executeUpdate();
        System.out.println("Suivi bien-être ajouté avec succès !");
    }

    public void modifier(SuiviBienEtre s) throws SQLException {
        String sql = "UPDATE suivi_bien_etre SET date_saisie=?, humeur=?, qualite_sommeil=?, niveau_energie=?, niveau_stress=?, qualite_alimentation=?, notes_libres=?, score=?, objectif_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, s.getDateSaisie());
        ps.setString(2, s.getHumeur());
        ps.setInt(3, s.getQualiteSommeil());
        ps.setInt(4, s.getNiveauEnergie());
        ps.setInt(5, s.getNiveauStress());
        ps.setInt(6, s.getQualiteAlimentation());
        ps.setString(7, s.getNotesLibres());
        ps.setDouble(8, s.getScore());
        ps.setInt(9, s.getObjectifId());
        ps.setInt(10, s.getId());

        ps.executeUpdate();
        System.out.println("Suivi bien-être modifié avec succès !");
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM suivi_bien_etre WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
        System.out.println("Suivi bien-être supprimé avec succès !");
    }

    public List<SuiviBienEtre> recuperer() throws SQLException {
        List<SuiviBienEtre> suivis = new ArrayList<>();

        String sql = "SELECT id, date_saisie, humeur, qualite_sommeil, niveau_energie, niveau_stress, qualite_alimentation, notes_libres, score, objectif_id FROM suivi_bien_etre";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            suivis.add(mapResultSetToSuivi(rs));
        }

        return suivis;
    }

    public List<SuiviBienEtre> recupererParObjectif(int objectifId) throws SQLException {
        List<SuiviBienEtre> suivis = new ArrayList<>();

        String sql = "SELECT id, date_saisie, humeur, qualite_sommeil, niveau_energie, niveau_stress, qualite_alimentation, notes_libres, score, objectif_id " +
                "FROM suivi_bien_etre WHERE objectif_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, objectifId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            suivis.add(mapResultSetToSuivi(rs));
        }

        return suivis;
    }

    // =========================
    // FRONTOFFICE - SECURISE
    // =========================

    public List<SuiviBienEtre> recupererParObjectifEtUser(int objectifId, int userId) throws SQLException {
        List<SuiviBienEtre> suivis = new ArrayList<>();

        String sql = "SELECT s.id, s.date_saisie, s.humeur, s.qualite_sommeil, s.niveau_energie, " +
                "s.niveau_stress, s.qualite_alimentation, s.notes_libres, s.score, s.objectif_id " +
                "FROM suivi_bien_etre s " +
                "INNER JOIN objectif_sante o ON s.objectif_id = o.id " +
                "WHERE s.objectif_id = ? AND o.user_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, objectifId);
        ps.setInt(2, userId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            suivis.add(mapResultSetToSuivi(rs));
        }

        return suivis;
    }

    public SuiviBienEtre recupererParIdEtUser(int suiviId, int userId) throws SQLException {
        String sql = "SELECT s.id, s.date_saisie, s.humeur, s.qualite_sommeil, s.niveau_energie, " +
                "s.niveau_stress, s.qualite_alimentation, s.notes_libres, s.score, s.objectif_id " +
                "FROM suivi_bien_etre s " +
                "INNER JOIN objectif_sante o ON s.objectif_id = o.id " +
                "WHERE s.id = ? AND o.user_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, suiviId);
        ps.setInt(2, userId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToSuivi(rs);
        }

        return null;
    }

    public boolean modifierParUser(SuiviBienEtre s, int userId) throws SQLException {
        String sql = "UPDATE suivi_bien_etre s " +
                "INNER JOIN objectif_sante o ON s.objectif_id = o.id " +
                "SET s.date_saisie = ?, s.humeur = ?, s.qualite_sommeil = ?, s.niveau_energie = ?, " +
                "s.niveau_stress = ?, s.qualite_alimentation = ?, s.notes_libres = ?, s.score = ?, s.objectif_id = ? " +
                "WHERE s.id = ? AND o.user_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, s.getDateSaisie());
        ps.setString(2, s.getHumeur());
        ps.setInt(3, s.getQualiteSommeil());
        ps.setInt(4, s.getNiveauEnergie());
        ps.setInt(5, s.getNiveauStress());
        ps.setInt(6, s.getQualiteAlimentation());
        ps.setString(7, s.getNotesLibres());
        ps.setDouble(8, s.getScore());
        ps.setInt(9, s.getObjectifId());
        ps.setInt(10, s.getId());
        ps.setInt(11, userId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Suivi modifié avec succès pour cet utilisateur !");
            return true;
        }

        System.out.println("Modification refusée : suivi introuvable ou non autorisé.");
        return false;
    }

    public boolean supprimerParUser(int suiviId, int userId) throws SQLException {
        String sql = "DELETE s FROM suivi_bien_etre s " +
                "INNER JOIN objectif_sante o ON s.objectif_id = o.id " +
                "WHERE s.id = ? AND o.user_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, suiviId);
        ps.setInt(2, userId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Suivi supprimé avec succès pour cet utilisateur !");
            return true;
        }

        System.out.println("Suppression refusée : suivi introuvable ou non autorisé.");
        return false;
    }

    // =========================
    // METHODE PRIVEE COMMUNE
    // =========================

    private SuiviBienEtre mapResultSetToSuivi(ResultSet rs) throws SQLException {
        SuiviBienEtre s = new SuiviBienEtre();
        s.setId(rs.getInt("id"));
        s.setDateSaisie(rs.getDate("date_saisie"));
        s.setHumeur(rs.getString("humeur"));
        s.setQualiteSommeil(rs.getInt("qualite_sommeil"));
        s.setNiveauEnergie(rs.getInt("niveau_energie"));
        s.setNiveauStress(rs.getInt("niveau_stress"));
        s.setQualiteAlimentation(rs.getInt("qualite_alimentation"));
        s.setNotesLibres(rs.getString("notes_libres"));
        s.setScore(rs.getDouble("score"));
        s.setObjectifId(rs.getInt("objectif_id"));
        return s;
    }
    public List<SuiviBienEtre> recupererParObjectifAvecTri(int objectifId, String tri) throws SQLException {
        List<SuiviBienEtre> suivis = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT id, date_saisie, humeur, qualite_sommeil, niveau_energie, niveau_stress, " +
                        "qualite_alimentation, notes_libres, score, objectif_id " +
                        "FROM suivi_bien_etre WHERE objectif_id = ? "
        );

        if ("date".equals(tri)) {
            sql.append("ORDER BY date_saisie DESC ");
        } else if ("score".equals(tri)) {
            sql.append("ORDER BY score DESC ");
        } else if ("humeur".equals(tri)) {
            sql.append("ORDER BY CASE ")
                    .append("WHEN humeur = 'EXCELLENT' THEN 1 ")
                    .append("WHEN humeur = 'BIEN' THEN 2 ")
                    .append("WHEN humeur = 'MOYEN' THEN 3 ")
                    .append("WHEN humeur = 'MAUVAIS' THEN 4 ")
                    .append("ELSE 5 END ");
        } else {
            sql.append("ORDER BY id DESC ");
        }

        PreparedStatement ps = cnx.prepareStatement(sql.toString());
        ps.setInt(1, objectifId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            suivis.add(mapResultSetToSuivi(rs));
        }

        return suivis;
    }
    public List<SuiviBienEtre> recupererParObjectifEtUserAvecTri(int objectifId, int userId, String tri) throws SQLException {
        List<SuiviBienEtre> suivis = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT s.id, s.date_saisie, s.humeur, s.qualite_sommeil, s.niveau_energie, " +
                        "s.niveau_stress, s.qualite_alimentation, s.notes_libres, s.score, s.objectif_id " +
                        "FROM suivi_bien_etre s " +
                        "INNER JOIN objectif_sante o ON s.objectif_id = o.id " +
                        "WHERE s.objectif_id = ? AND o.user_id = ? "
        );

        if ("date".equals(tri)) {
            sql.append("ORDER BY s.date_saisie DESC ");
        } else if ("score".equals(tri)) {
            sql.append("ORDER BY s.score DESC ");
        } else if ("humeur".equals(tri)) {
            sql.append("ORDER BY CASE ")
                    .append("WHEN s.humeur = 'EXCELLENT' THEN 1 ")
                    .append("WHEN s.humeur = 'BIEN' THEN 2 ")
                    .append("WHEN s.humeur = 'MOYEN' THEN 3 ")
                    .append("WHEN s.humeur = 'MAUVAIS' THEN 4 ")
                    .append("ELSE 5 END ");
        } else {
            sql.append("ORDER BY s.id DESC ");
        }

        PreparedStatement ps = cnx.prepareStatement(sql.toString());
        ps.setInt(1, objectifId);
        ps.setInt(2, userId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            suivis.add(mapResultSetToSuivi(rs));
        }

        return suivis;
    }
    public SuiviBienEtre getDernierSuiviByObjectifId(int objectifId) throws SQLException {
        String sql = "SELECT id, date_saisie, humeur, qualite_sommeil, niveau_energie, " +
                "niveau_stress, qualite_alimentation, notes_libres, score, objectif_id " +
                "FROM suivi_bien_etre " +
                "WHERE objectif_id = ? " +
                "ORDER BY date_saisie DESC, id DESC " +
                "LIMIT 1";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, objectifId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToSuivi(rs);
        }

        return null;
    }
}