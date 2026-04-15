package piJava.entities;

public class Groupe {

    private int id;
    private String nom;
    private String projet;
    private int nbreMembre;
    private String statut;
    private String description;

    // ─── Full Constructor ───────────────────────────────────────
    public Groupe(int id, String nom, String projet, int nbreMembre, 
                  String statut, String description) {
        this.id = id;
        this.nom = nom;
        this.projet = projet;
        this.nbreMembre = nbreMembre;
        this.statut = statut;
        this.description = description;
    }

    // ─── Minimal Constructor (for creation) ─────────────────────
    public Groupe(String nom, String projet, int nbreMembre) {
        this.nom = nom;
        this.projet = projet;
        this.nbreMembre = nbreMembre;
        this.statut = "Actif";
    }

    // ─── Empty Constructor ───────────────────────────────────────
    public Groupe() {}

    // ─── Getters & Setters ───────────────────────────────────────
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

    public String getProjet() {
        return projet;
    }

    public void setProjet(String projet) {
        this.projet = projet;
    }

    public int getNbreMembre() {
        return nbreMembre;
    }

    public void setNbreMembre(int nbreMembre) {
        this.nbreMembre = nbreMembre;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Groupe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", projet='" + projet + '\'' +
                ", nbreMembre=" + nbreMembre +
                ", statut='" + statut + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
