package piJava.services;

import piJava.entities.ObjectifSante;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectifSanteService {

    private Connection cnx;

    public ObjectifSanteService() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    public void ajouter(ObjectifSante o) throws SQLException {
        String sql = "INSERT INTO objectif_sante (titre, type, valeur_cible, date_debut, date_fin, priorite, statut, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, o.getTitre());
        ps.setString(2, o.getType());
        ps.setInt(3, o.getValeurCible());
        ps.setDate(4, o.getDateDebut());
        ps.setDate(5, o.getDateFin());
        ps.setString(6, o.getPriorite());
        ps.setString(7, o.getStatut());
        ps.setInt(8, o.getUserId());

        ps.executeUpdate();
        System.out.println("Objectif ajouté avec succès !");
    }

    public void modifier(ObjectifSante o) throws SQLException {
        String sql = "UPDATE objectif_sante SET titre=?, type=?, valeur_cible=?, date_debut=?, date_fin=?, priorite=?, statut=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, o.getTitre());
        ps.setString(2, o.getType());
        ps.setInt(3, o.getValeurCible());
        ps.setDate(4, o.getDateDebut());
        ps.setDate(5, o.getDateFin());
        ps.setString(6, o.getPriorite());
        ps.setString(7, o.getStatut());
        ps.setInt(8, o.getId());

        ps.executeUpdate();
        System.out.println("Objectif modifié avec succès !");
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM objectif_sante WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
        System.out.println("Objectif supprimé avec succès !");
    }

    public List<ObjectifSante> recuperer() throws SQLException {
        List<ObjectifSante> objectifs = new ArrayList<>();

        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            objectifs.add(mapResultSetToObjectif(rs));
        }

        return objectifs;
    }

    public ObjectifSante recupererParId(int id) throws SQLException {
        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id " +
                "WHERE o.id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToObjectif(rs);
        }

        return null;
    }

    // =========================
    // FRONTOFFICE - SECURISE
    // =========================

    public List<ObjectifSante> recupererParUser(int userId) throws SQLException {
        List<ObjectifSante> objectifs = new ArrayList<>();

        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id " +
                "WHERE o.user_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            objectifs.add(mapResultSetToObjectif(rs));
        }

        return objectifs;
    }

    public ObjectifSante recupererParIdEtUser(int idObjectif, int userId) throws SQLException {
        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id " +
                "WHERE o.id = ? AND o.user_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idObjectif);
        ps.setInt(2, userId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapResultSetToObjectif(rs);
        }

        return null;
    }

    public boolean modifierParUser(ObjectifSante o, int userId) throws SQLException {
        String sql = "UPDATE objectif_sante " +
                "SET titre=?, type=?, valeur_cible=?, date_debut=?, date_fin=?, priorite=?, statut=? " +
                "WHERE id=? AND user_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, o.getTitre());
        ps.setString(2, o.getType());
        ps.setInt(3, o.getValeurCible());
        ps.setDate(4, o.getDateDebut());
        ps.setDate(5, o.getDateFin());
        ps.setString(6, o.getPriorite());
        ps.setString(7, o.getStatut());
        ps.setInt(8, o.getId());
        ps.setInt(9, userId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Objectif modifié avec succès pour cet utilisateur !");
            return true;
        }

        System.out.println("Modification refusée : objectif introuvable ou non autorisé.");
        return false;
    }

    public boolean supprimerParUser(int idObjectif, int userId) throws SQLException {
        String sql = "DELETE FROM objectif_sante WHERE id=? AND user_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idObjectif);
        ps.setInt(2, userId);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Objectif supprimé avec succès pour cet utilisateur !");
            return true;
        }

        System.out.println("Suppression refusée : objectif introuvable ou non autorisé.");
        return false;
    }

    // =========================
    // METHODE PRIVEE COMMUNE
    // =========================

    private ObjectifSante mapResultSetToObjectif(ResultSet rs) throws SQLException {
        ObjectifSante o = new ObjectifSante();
        o.setId(rs.getInt("id"));
        o.setTitre(rs.getString("titre"));
        o.setType(rs.getString("type"));
        o.setValeurCible(rs.getInt("valeur_cible"));
        o.setDateDebut(rs.getDate("date_debut"));
        o.setDateFin(rs.getDate("date_fin"));
        o.setPriorite(rs.getString("priorite"));
        o.setStatut(rs.getString("statut"));
        o.setUserId(rs.getInt("user_id"));
        o.setUserNom(rs.getString("user_nom"));
        o.setUserPrenom(rs.getString("user_prenom"));
        return o;
    }
    public List<ObjectifSante> rechercherEtTrierBack(String recherche, String categorie, String tri) throws SQLException {
        List<ObjectifSante> objectifs = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                        "o.priorite, o.statut, o.user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                        "FROM objectif_sante o " +
                        "LEFT JOIN user u ON o.user_id = u.id " +
                        "WHERE 1=1 "
        );

        boolean hasRecherche = recherche != null && !recherche.trim().isEmpty();
        boolean hasCategorie = categorie != null && !categorie.trim().isEmpty() && !categorie.equals("Toutes");

        if (hasRecherche) {
            sql.append("AND (");
            sql.append("LOWER(o.titre) LIKE ? ");
            sql.append("OR LOWER(u.nom) LIKE ? ");

            if (recherche.matches("\\d+")) {
                sql.append("OR o.user_id = ? OR o.id = ? ");
            }

            sql.append(") ");
        }

        if (hasCategorie) {
            sql.append("AND o.type = ? ");
        }

        if ("date_debut".equals(tri)) {
            sql.append("ORDER BY o.date_debut DESC ");
        } else if ("priorite".equals(tri)) {
            sql.append("ORDER BY CASE ")
                    .append("WHEN o.priorite = 'HAUTE' THEN 1 ")
                    .append("WHEN o.priorite = 'MOYENNE' THEN 2 ")
                    .append("WHEN o.priorite = 'BASSE' THEN 3 ")
                    .append("ELSE 4 END, o.date_debut DESC ");
        } else {
            sql.append("ORDER BY o.id DESC ");
        }

        PreparedStatement ps = cnx.prepareStatement(sql.toString());

        int index = 1;

        if (hasRecherche) {
            String keyword = "%" + recherche.trim().toLowerCase() + "%";
            ps.setString(index++, keyword);
            ps.setString(index++, keyword);

            if (recherche.matches("\\d+")) {
                int value = Integer.parseInt(recherche.trim());
                ps.setInt(index++, value);
                ps.setInt(index++, value);
            }
        }

        if (hasCategorie) {
            ps.setString(index++, categorie);
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            objectifs.add(mapResultSetToObjectif(rs));
        }

        return objectifs;
    }
    public List<ObjectifSante> rechercherEtTrierFront(int userId, String recherche, String categorie, String tri) throws SQLException {
        List<ObjectifSante> objectifs = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                        "o.priorite, o.statut, o.user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                        "FROM objectif_sante o " +
                        "LEFT JOIN user u ON o.user_id = u.id " +
                        "WHERE o.user_id = ? "
        );

        boolean hasRecherche = recherche != null && !recherche.trim().isEmpty();
        boolean hasCategorie = categorie != null && !categorie.trim().isEmpty() && !categorie.equals("Toutes");

        if (hasRecherche) {
            sql.append("AND LOWER(o.titre) LIKE ? ");
        }

        if (hasCategorie) {
            sql.append("AND o.type = ? ");
        }

        if ("date_debut".equals(tri)) {
            sql.append("ORDER BY o.date_debut DESC ");
        } else if ("priorite".equals(tri)) {
            sql.append("ORDER BY CASE ")
                    .append("WHEN o.priorite = 'HAUTE' THEN 1 ")
                    .append("WHEN o.priorite = 'MOYENNE' THEN 2 ")
                    .append("WHEN o.priorite = 'BASSE' THEN 3 ")
                    .append("ELSE 4 END, o.date_debut DESC ");
        } else {
            sql.append("ORDER BY o.id DESC ");
        }

        PreparedStatement ps = cnx.prepareStatement(sql.toString());
        int index = 1;
        ps.setInt(index++, userId);

        if (hasRecherche) {
            ps.setString(index++, "%" + recherche.trim().toLowerCase() + "%");
        }

        if (hasCategorie) {
            ps.setString(index++, categorie);
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            objectifs.add(mapResultSetToObjectif(rs));
        }

        return objectifs;
    }
}