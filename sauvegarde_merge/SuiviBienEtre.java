package piJava.entities;

import java.sql.Date;

public class SuiviBienEtre {

    private int id;
    private Date dateSaisie;
    private String humeur;
    private int qualiteSommeil;
    private int niveauEnergie;
    private int niveauStress;
    private int qualiteAlimentation;
    private String notesLibres;
    private double score;
    private int objectifId;

    public SuiviBienEtre() {
    }

    public SuiviBienEtre(int id, Date dateSaisie, String humeur, int qualiteSommeil, int niveauEnergie, int niveauStress,
                         int qualiteAlimentation, String notesLibres, double score, int objectifId) {
        this.id = id;
        this.dateSaisie = dateSaisie;
        this.humeur = humeur;
        this.qualiteSommeil = qualiteSommeil;
        this.niveauEnergie = niveauEnergie;
        this.niveauStress = niveauStress;
        this.qualiteAlimentation = qualiteAlimentation;
        this.notesLibres = notesLibres;
        this.score = score;
        this.objectifId = objectifId;
    }

    public SuiviBienEtre(Date dateSaisie, String humeur, int qualiteSommeil, int niveauEnergie, int niveauStress,
                         int qualiteAlimentation, String notesLibres, double score, int objectifId) {
        this.dateSaisie = dateSaisie;
        this.humeur = humeur;
        this.qualiteSommeil = qualiteSommeil;
        this.niveauEnergie = niveauEnergie;
        this.niveauStress = niveauStress;
        this.qualiteAlimentation = qualiteAlimentation;
        this.notesLibres = notesLibres;
        this.score = score;
        this.objectifId = objectifId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Date getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(Date dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public String getHumeur() {
        return humeur;
    }

    public void setHumeur(String humeur) {
        this.humeur = humeur;
    }

    public int getQualiteSommeil() {
        return qualiteSommeil;
    }

    public void setQualiteSommeil(int qualiteSommeil) {
        this.qualiteSommeil = qualiteSommeil;
    }

    public int getNiveauEnergie() {
        return niveauEnergie;
    }

    public void setNiveauEnergie(int niveauEnergie) {
        this.niveauEnergie = niveauEnergie;
    }

    public int getNiveauStress() {
        return niveauStress;
    }

    public void setNiveauStress(int niveauStress) {
        this.niveauStress = niveauStress;
    }

    public int getQualiteAlimentation() {
        return qualiteAlimentation;
    }

    public void setQualiteAlimentation(int qualiteAlimentation) {
        this.qualiteAlimentation = qualiteAlimentation;
    }

    public String getNotesLibres() {
        return notesLibres;
    }

    public void setNotesLibres(String notesLibres) {
        this.notesLibres = notesLibres;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getObjectifId() {
        return objectifId;
    }

    public void setObjectifId(int objectifId) {
        this.objectifId = objectifId;
    }

    @Override
    public String toString() {
        return "SuiviBienEtre{" +
                "id=" + id +
                ", dateSaisie=" + dateSaisie +
                ", humeur='" + humeur + '\'' +
                ", qualiteSommeil=" + qualiteSommeil +
                ", niveauEnergie=" + niveauEnergie +
                ", niveauStress=" + niveauStress +
                ", qualiteAlimentation=" + qualiteAlimentation +
                ", notesLibres='" + notesLibres + '\'' +
                ", score=" + score +
                ", objectifId=" + objectifId +
                '}';
    }
}