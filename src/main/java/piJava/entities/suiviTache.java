package piJava.entities;

import java.util.Date;

public class suiviTache {

    private int id;

    // Many-to-one relationship with Tache
    private tache tache;

    // Date of action
    private Date dateAction;

    // Old status before the change
    private String ancienStatut; // nullable

    // New status after the change
    private String nouveauStatut; // nullable

    // Optional comment
    private String commentaire; // nullable

    // --------- Constructors ---------
    public suiviTache() {}

    public suiviTache(tache tache, Date dateAction, String ancienStatut, String nouveauStatut, String commentaire) {
        this.tache = tache;
        this.dateAction = dateAction;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.commentaire = commentaire;
    }

    // --------- Getters & Setters ---------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public tache getTache() {
        return tache;
    }

    public void setTache(tache tache) {
        this.tache= tache;
    }

    public Date getDateAction() {
        return dateAction;
    }

    public void setDateAction(Date dateAction) {
        this.dateAction = dateAction;
    }

    public String getAncienStatut() {
        return ancienStatut;
    }

    public void setAncienStatut(String ancienStatut) {
        this.ancienStatut = ancienStatut;
    }

    public String getNouveauStatut() {
        return nouveauStatut;
    }

    public void setNouveauStatut(String nouveauStatut) {
        this.nouveauStatut = nouveauStatut;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}