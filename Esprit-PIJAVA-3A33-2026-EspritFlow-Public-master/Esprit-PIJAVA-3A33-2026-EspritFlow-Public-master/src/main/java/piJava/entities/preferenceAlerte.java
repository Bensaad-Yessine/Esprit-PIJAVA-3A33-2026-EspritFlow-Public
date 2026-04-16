package piJava.entities;

import java.time.LocalTime;
import java.util.Date;

public class preferenceAlerte {
    private int id;
    private String nom;
    private Boolean is_active;
    private Boolean is_default;
    private Boolean email_actif;
    private Boolean push_actif;
    private Boolean site_notif_active;
    private int delai_rappel_min;
    private LocalTime heure_silence_debut;
    private LocalTime heure_silence_fin;
    private Date date_creation;
    private Date date_mise_ajour;
    private int user_id;
    private String description;



    public preferenceAlerte() {}
    public preferenceAlerte(String nom, Boolean is_active, Boolean is_default, Boolean email_actif, Boolean push_actif, Boolean site_notif_active, int delai_rappel_min, LocalTime heure_silence_debut, LocalTime heure_silence_fin, Date date_creation, Date date_mise_ajour, int user_id) {
        this.nom = nom;
        this.is_active = is_active;
        this.is_default = is_default;
        this.email_actif = email_actif;
        this.push_actif = push_actif;
        this.site_notif_active = site_notif_active;
        this.delai_rappel_min = delai_rappel_min;
        this.heure_silence_debut = heure_silence_debut;
        this.heure_silence_fin = heure_silence_fin;
        this.date_creation = date_creation;
        this.date_mise_ajour = date_mise_ajour;
        this.user_id = user_id;
    }



    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public Boolean getIs_active() {
        return is_active;
    }
    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }
    public Boolean getIs_default() {
        return is_default;
    }
    public void setIs_default(Boolean is_default) {
        this.is_default = is_default;
    }
    public Boolean getEmail_actif() {
        return email_actif;
    }
    public void setEmail_actif(Boolean email_actif) {
        this.email_actif = email_actif;
    }
    public Boolean getPush_actif() {
        return push_actif;
    }
    public void setPush_actif(Boolean push_actif) {
        this.push_actif = push_actif;
    }
    public Boolean getSite_notif_active() {
        return site_notif_active;
    }
    public void setSite_notif_active(Boolean site_notif_active) {
        this.site_notif_active = site_notif_active;
    }
    public int getDelai_rappel_min() {
        return delai_rappel_min;
    }
    public void setDelai_rappel_min(int delai_rappel_min) {
        this.delai_rappel_min = delai_rappel_min;
    }
    public LocalTime getHeure_silence_debut() {
        return heure_silence_debut;
    }
    public void setHeure_silence_debut(LocalTime heure_silence_debut) {
        this.heure_silence_debut = heure_silence_debut;
    }
    public LocalTime getHeure_silence_fin() {
        return heure_silence_fin;
    }
    public void setHeure_silence_fin(LocalTime heure_silence_fin) {
        this.heure_silence_fin = heure_silence_fin;
    }
    public Date getDate_creation() {
        return date_creation;
    }
    public void setDate_creation(Date date_creation) {
        this.date_creation = date_creation;
    }
    public Date getDate_mise_ajour() {
        return date_mise_ajour;
    }
    public void setDate_mise_ajour(Date date_mise_ajour) {
        this.date_mise_ajour = date_mise_ajour;
    }
    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "preferenceAlerte{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", is_active=" + is_active +
                ", is_default=" + is_default +
                ", email_actif=" + email_actif +
                ", push_actif=" + push_actif +
                ", site_notif_active=" + site_notif_active +
                ", delai_rappel_min=" + delai_rappel_min +
                ", heure_silence_debut=" + heure_silence_debut +
                ", heure_silence_fin=" + heure_silence_fin +
                ", date_creation=" + date_creation +
                ", date_mise_ajour=" + date_mise_ajour +
                ", user_id=" + user_id +
                '}';
    }

}
