package piJava.entities;

public class Classe {

    private int id;
    private String nom;
    private String niveau;
    private String anneeUniversitaire;
    private String description;
    private String filiere;
    private Integer userId; // nullable → Integer not int

    // ─── Full Constructor ───────────────────────────────────────
    public Classe(int id, String nom, String niveau, String anneeUniversitaire,
                  String description, String filiere, Integer userId) {
        this.id = id;
        this.nom = nom;
        this.niveau = niveau;
        this.anneeUniversitaire = anneeUniversitaire;
        this.description = description;
        this.filiere = filiere;
        this.userId = userId;
    }

    // ─── Minimal Constructor (for creation) ─────────────────────
    public Classe(String nom, String niveau, String anneeUniversitaire) {
        this.nom = nom;
        this.niveau = niveau;
        this.anneeUniversitaire = anneeUniversitaire;
    }

    // ─── Empty Constructor ───────────────────────────────────────
    public Classe() {}

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

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getAnneeUniversitaire() {
        return anneeUniversitaire;
    }

    public void setAnneeUniversitaire(String anneeUniversitaire) {
        this.anneeUniversitaire = anneeUniversitaire;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // ─── toString (useful for debugging) ────────────────────────
    @Override
    public String toString() {
        return  nom ;
    }

    public String displayClasse(){
        return "Classe{id=" + id + ", nom='" + nom + "', niveau='" + niveau +
                "', anneeUniversitaire='" + anneeUniversitaire + "', filiere='" + filiere +
                "', userId=" + userId + "}";
    }
}