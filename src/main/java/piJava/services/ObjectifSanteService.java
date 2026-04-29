package piJava.services;

import piJava.entities.ObjectifSante;
import piJava.entities.SuiviBienEtre;
import piJava.services.api.BrevoEmailService;
import piJava.services.api.PdfArchiveObjectifService;
import piJava.utils.MyDataBase;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ObjectifSanteService {

    private static final double SEUIL_REUSSITE = 75.0; // mets 70.0 si tu veux comme le web
    private final Connection cnx;

    private final SuiviBienEtreService suiviService = new SuiviBienEtreService();
    private final PdfArchiveObjectifService pdfService = new PdfArchiveObjectifService();
    private final BrevoEmailService brevoEmailService = new BrevoEmailService();

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
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM objectif_sante WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<ObjectifSante> recuperer() throws SQLException {
        mettreAJourEtArchiverObjectifsDepasses();

        List<ObjectifSante> objectifs = new ArrayList<>();
        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                "o.share_expires_at, o.archive_email_sent_at, " +
                "u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id " +
                "WHERE o.archived_at IS NULL";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            objectifs.add(mapResultSetToObjectif(rs));
        }

        return objectifs;
    }

    public ObjectifSante recupererParId(int id) throws SQLException {
        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                "o.share_expires_at, o.archive_email_sent_at, " +
                "u.nom AS user_nom, u.prenom AS user_prenom " +
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

    public List<ObjectifSante> recupererParUser(int userId) throws SQLException {
        mettreAJourEtArchiverObjectifsDepasses();

        List<ObjectifSante> objectifs = new ArrayList<>();

        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                "o.share_expires_at, o.archive_email_sent_at, " +
                "u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id " +
                "WHERE o.user_id = ? AND o.archived_at IS NULL";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            objectifs.add(mapResultSetToObjectif(rs));
        }

        return objectifs;
    }

    public List<ObjectifSante> recupererArchivesParUser(int userId) throws SQLException {
        mettreAJourEtArchiverObjectifsDepasses();

        List<ObjectifSante> objectifs = new ArrayList<>();

        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                "o.share_expires_at, o.archive_email_sent_at, " +
                "u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id " +
                "WHERE o.user_id = ? AND o.archived_at IS NOT NULL " +
                "ORDER BY o.archived_at DESC";

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
                "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                "o.share_expires_at, o.archive_email_sent_at, " +
                "u.nom AS user_nom, u.prenom AS user_prenom " +
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
                "WHERE id=? AND user_id=? AND archived_at IS NULL";

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

        return ps.executeUpdate() > 0;
    }

    public boolean supprimerParUser(int idObjectif, int userId) throws SQLException {
        String sql = "DELETE FROM objectif_sante WHERE id=? AND user_id=? AND archived_at IS NULL";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, idObjectif);
        ps.setInt(2, userId);

        return ps.executeUpdate() > 0;
    }

    public List<ObjectifSante> rechercherEtTrierBack(String recherche, String categorie, String tri) throws SQLException {
        mettreAJourEtArchiverObjectifsDepasses();

        List<ObjectifSante> objectifs = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                        "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                        "o.share_expires_at, o.archive_email_sent_at, " +
                        "u.nom AS user_nom, u.prenom AS user_prenom " +
                        "FROM objectif_sante o " +
                        "LEFT JOIN user u ON o.user_id = u.id " +
                        "WHERE o.archived_at IS NULL "
        );

        boolean hasRecherche = recherche != null && !recherche.trim().isEmpty();
        boolean hasCategorie = categorie != null && !categorie.trim().isEmpty() && !categorie.equals("Toutes");

        if (hasRecherche) {
            sql.append("AND (LOWER(o.titre) LIKE ? OR LOWER(u.nom) LIKE ? ");
            if (recherche.matches("\\d+")) {
                sql.append("OR o.user_id = ? OR o.id = ? ");
            }
            sql.append(") ");
        }

        if (hasCategorie) {
            sql.append("AND o.type = ? ");
        }

        ajouterTri(sql, tri);

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
        mettreAJourEtArchiverObjectifsDepasses();

        List<ObjectifSante> objectifs = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                        "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                        "o.share_expires_at, o.archive_email_sent_at, " +
                        "u.nom AS user_nom, u.prenom AS user_prenom " +
                        "FROM objectif_sante o " +
                        "LEFT JOIN user u ON o.user_id = u.id " +
                        "WHERE o.user_id = ? AND o.archived_at IS NULL "
        );

        boolean hasRecherche = recherche != null && !recherche.trim().isEmpty();
        boolean hasCategorie = categorie != null && !categorie.trim().isEmpty() && !categorie.equals("Toutes");

        if (hasRecherche) {
            sql.append("AND LOWER(o.titre) LIKE ? ");
        }

        if (hasCategorie) {
            sql.append("AND o.type = ? ");
        }

        ajouterTri(sql, tri);

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

    public void mettreAJourEtArchiverObjectifsDepasses() throws SQLException {
        String sql = "SELECT id, date_fin FROM objectif_sante WHERE archived_at IS NULL";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        LocalDate today = LocalDate.now();

        while (rs.next()) {
            int objectifId = rs.getInt("id");
            Date dateFin = rs.getDate("date_fin");

            if (dateFin != null && !dateFin.toLocalDate().isAfter(today)) {
                double scoreMoyen = calculerScoreMoyenObjectif(objectifId);
                String nouveauStatut = scoreMoyen >= SEUIL_REUSSITE ? "ATTEINT" : "ABANDONNE";

                boolean archiveOk = archiverObjectif(objectifId, nouveauStatut);

                if (archiveOk) {
                    try {
                        ObjectifSante objectifArchive = recupererParId(objectifId);
                        envoyerEmailArchive(objectifArchive);
                    } catch (Exception e) {
                        System.out.println("Erreur lors de l'envoi email Brevo : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public double calculerScoreMoyenObjectif(int objectifId) throws SQLException {
        String sql = "SELECT AVG(score) AS moyenne FROM suivi_bien_etre WHERE objectif_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, objectifId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            double moyenne = rs.getDouble("moyenne");
            if (rs.wasNull()) {
                return 0;
            }
            return moyenne;
        }
        return 0;
    }

    public boolean archiverObjectif(int objectifId, String statutFinal) throws SQLException {
        String sql = "UPDATE objectif_sante SET statut = ?, archived_at = NOW() WHERE id = ? AND archived_at IS NULL";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, statutFinal);
        ps.setInt(2, objectifId);
        return ps.executeUpdate() > 0;
    }

    private String recupererEmailUserParObjectif(int objectifId) throws SQLException {
        String sql = "SELECT u.email " +
                "FROM objectif_sante o " +
                "JOIN user u ON o.user_id = u.id " +
                "WHERE o.id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, objectifId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("email");
        }
        return null;
    }

    private void mettreAJourArchivePdfPath(int objectifId, String cheminPdf) throws SQLException {
        String sql = "UPDATE objectif_sante SET archive_pdf_path = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, cheminPdf);
        ps.setInt(2, objectifId);
        ps.executeUpdate();
    }

    private void marquerEmailArchiveEnvoye(int objectifId) throws SQLException {
        String sql = "UPDATE objectif_sante SET archive_email_sent_at = NOW() WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, objectifId);
        ps.executeUpdate();
    }

    private void envoyerEmailArchive(ObjectifSante objectif) throws Exception {
        if (objectif == null) {
            return;
        }

        if (objectif.getArchiveEmailSentAt() != null) {
            return;
        }

        String toEmail = recupererEmailUserParObjectif(objectif.getId());
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        List<SuiviBienEtre> suivis = suiviService.recupererParObjectif(objectif.getId());
        File pdfFile = pdfService.genererPdf(objectif, suivis);

        if (pdfFile == null || !pdfFile.exists()) {
            return;
        }

        mettreAJourArchivePdfPath(objectif.getId(), pdfFile.getAbsolutePath());

        String html = """
                <h2>Votre objectif est archivé ✅</h2>
                <p>L'objectif <b>%s</b> est maintenant terminé.</p>
                <p>Le rapport PDF est envoyé en pièce jointe.</p>
                <br>
                <p><b>Objectif Santé</b></p>
                """.formatted(objectif.getTitre());

        brevoEmailService.sendPdfWithAttachment(
                toEmail,
                "Rapport objectif archivé",
                html,
                pdfFile,
                "objectif_" + objectif.getId() + ".pdf"
        );

        marquerEmailArchiveEnvoye(objectif.getId());
    }

    private void ajouterTri(StringBuilder sql, String tri) {
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
    }

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
        o.setArchivedAt(rs.getTimestamp("archived_at"));
        o.setArchivePdfPath(rs.getString("archive_pdf_path"));
        o.setShareToken(rs.getString("share_token"));
        o.setShareExpiresAt(rs.getTimestamp("share_expires_at"));
        o.setArchiveEmailSentAt(rs.getTimestamp("archive_email_sent_at"));
        return o;
    }

    public List<ObjectifSante> recupererArchivesBack() throws SQLException {
        List<ObjectifSante> objectifs = new ArrayList<>();

        String sql = "SELECT o.id, o.titre, o.type, o.valeur_cible, o.date_debut, o.date_fin, " +
                "o.priorite, o.statut, o.user_id, o.archived_at, o.archive_pdf_path, o.share_token, " +
                "o.share_expires_at, o.archive_email_sent_at, " +
                "u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM objectif_sante o " +
                "LEFT JOIN user u ON o.user_id = u.id " +
                "WHERE o.archived_at IS NOT NULL " +
                "ORDER BY o.archived_at DESC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            objectifs.add(mapResultSetToObjectif(rs));
        }

        return objectifs;
    }
    public void desarchiverObjectif(int objectifId) throws SQLException {
        String sql = "UPDATE objectif_sante " +
                "SET archived_at = NULL, archive_pdf_path = NULL, archive_email_sent_at = NULL, statut = 'EN_COURS' " +
                "WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, objectifId);
        ps.executeUpdate();
    }
    public void mettreAJourApresModificationArchive(ObjectifSante objectif) throws SQLException {
        if (objectif == null || objectif.getDateFin() == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate dateFin = objectif.getDateFin().toLocalDate();

        if (dateFin.isAfter(today)) {
            desarchiverObjectif(objectif.getId());
        } else {
            double scoreMoyen = calculerScoreMoyenObjectif(objectif.getId());
            String nouveauStatut = scoreMoyen >= SEUIL_REUSSITE ? "ATTEINT" : "ABANDONNE";

            String sql = "UPDATE objectif_sante SET statut = ?, archived_at = NOW() WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, nouveauStatut);
            ps.setInt(2, objectif.getId());
            ps.executeUpdate();
        }
    }
}