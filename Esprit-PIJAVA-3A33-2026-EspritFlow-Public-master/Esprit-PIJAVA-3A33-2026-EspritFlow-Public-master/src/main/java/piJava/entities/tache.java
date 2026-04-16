package piJava.entities;


import java.util.Date;

public class tache {

    private int id;
    private String titre;
    private String type;
    private Date date_debut;
    private Date date_fin;
    private String priorite;
    private String statut;
    private int user_id;
    private Date created_at;
    private Date date_echeance;
    private String description;
    private int duree_estimee;
    private Date updated_at;
    private double prediction;


    public tache() {}
    public tache(String titre, String type, Date date_debut, Date date_fin,
                 String priorite, String statut, int user_id) {

        this.titre = titre;
        this.type = type;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.priorite = priorite;
        this.statut = statut;
        this.user_id = user_id;
        this.created_at = new Date();
        this.updated_at= new Date();
    }


    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Date getDate_debut() {
        return date_debut;
    }
    public void setDate_debut(Date date_debut) {
        this.date_debut = date_debut;
    }
    public Date getDate_fin() {
        return date_fin;
    }
    public void setDate_fin(Date date_fin) {
        this.date_fin = date_fin;
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
    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    public Date getCreated_at() {
        return created_at;
    }
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
    public Date getDate_echeance() {
        return date_echeance;
    }
    public void setDate_echeance(Date date_echeance) {
        this.date_echeance = date_echeance;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getDuree_estimee() {
        return duree_estimee;
    }
    public void setDuree_estimee(int duree_estimee) {
        this.duree_estimee = duree_estimee;
    }
    public Date getUpdated_at() {
        return updated_at;
    }
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
    public double getPrediction() {
        return prediction;
    }
    public void setPrediction(double prediction) {
        this.prediction = prediction;
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

    @Override
    public String toString() {
        return "tache{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                ", priorite='" + priorite + '\'' +
                ", statut='" + statut + '\'' +
                ", user_id=" + user_id +
                ", created_at=" + created_at +
                ", date_echeance=" + date_echeance +
                ", description='" + description + '\'' +
                ", duree_estimee=" + duree_estimee +
                ", updated_at=" + updated_at +
                ", prediction=" + prediction +
                "}\n";
    }
}
