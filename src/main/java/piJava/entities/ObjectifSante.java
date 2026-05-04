package piJava.entities;

import java.sql.Date;
import java.sql.Timestamp;

public class ObjectifSante {

    private int id;
    private String titre;
    private String type;
    private int valeurCible;
    private Date dateDebut;
    private Date dateFin;
    private String priorite;
    private String statut;
    private int userId;
    private String userNom;
    private String userPrenom;
    private Timestamp archiveEmailSentAt;
    private Timestamp archivedAt;
    private String archivePdfPath;
    private String shareToken;
    private Timestamp shareExpiresAt;

    public ObjectifSante() {
    }

    public ObjectifSante(int id, String titre, String type, int valeurCible, Date dateDebut, Date dateFin,
                         String priorite, String statut, int userId) {
        this.id = id;
        this.titre = titre;
        this.type = type;
        this.valeurCible = valeurCible;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.priorite = priorite;
        this.statut = statut;
        this.userId = userId;
    }

    public ObjectifSante(String titre, String type, int valeurCible, Date dateDebut, Date dateFin,
                         String priorite, String statut, int userId) {
        this.titre = titre;
        this.type = type;
        this.valeurCible = valeurCible;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.priorite = priorite;
        this.statut = statut;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getValeurCible() {
        return valeurCible;
    }

    public void setValeurCible(int valeurCible) {
        this.valeurCible = valeurCible;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserNom() {
        return userNom;
    }

    public void setUserNom(String userNom) {
        this.userNom = userNom;
    }

    public String getUserPrenom() {
        return userPrenom;
    }

    public void setUserPrenom(String userPrenom) {
        this.userPrenom = userPrenom;
    }
    public Timestamp getArchiveEmailSentAt() {
        return archiveEmailSentAt;
    }

    public void setArchiveEmailSentAt(Timestamp archiveEmailSentAt) {
        this.archiveEmailSentAt = archiveEmailSentAt;
    }

    public Timestamp getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Timestamp archivedAt) {
        this.archivedAt = archivedAt;
    }

    public String getArchivePdfPath() {
        return archivePdfPath;
    }

    public void setArchivePdfPath(String archivePdfPath) {
        this.archivePdfPath = archivePdfPath;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public Timestamp getShareExpiresAt() {
        return shareExpiresAt;
    }

    public void setShareExpiresAt(Timestamp shareExpiresAt) {
        this.shareExpiresAt = shareExpiresAt;
    }

    @Override
    public String toString() {
        return "ObjectifSante{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", valeurCible=" + valeurCible +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", priorite='" + priorite + '\'' +
                ", statut='" + statut + '\'' +
                ", userId=" + userId +
                ", archivedAt=" + archivedAt +
                '}';
    }
}