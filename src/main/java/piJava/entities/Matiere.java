package piJava.entities;

import java.util.ArrayList;
import java.util.List;

public class Matiere {

    private int id;
    private double coefficient;
    private int chargehoraire;
    private int scorecomplexite;
    private String nom;
    private String description;

    // ─── Many-to-Many relation with Classe ──────────────────────
    private List<Classe> classes = new ArrayList<>();

    // ─── Full Constructor ────────────────────────────────────────
    public Matiere(int id, double coefficient, int chargehoraire, int scorecomplexite,
                   String nom, String description) {
        this.id = id;
        this.coefficient = coefficient;
        this.chargehoraire = chargehoraire;
        this.scorecomplexite = scorecomplexite;
        this.nom = nom;
        this.description = description;
    }

    // ─── Minimal Constructor (for creation) ─────────────────────
    public Matiere(String nom, double coefficient, int chargehoraire) {
        this.nom = nom;
        this.coefficient = coefficient;
        this.chargehoraire = chargehoraire;
    }

    // ─── Empty Constructor ───────────────────────────────────────
    public Matiere() {}

    // ─── Getters & Setters ───────────────────────────────────────
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public int getChargehoraire() {
        return chargehoraire;
    }

    public void setChargehoraire(int chargehoraire) {
        this.chargehoraire = chargehoraire;
    }

    public int getScorecomplexite() {
        return scorecomplexite;
    }

    public void setScorecomplexite(int scorecomplexite) {
        this.scorecomplexite = scorecomplexite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Classe> getClasses() {
        return classes;
    }

    public void setClasses(List<Classe> classes) {
        this.classes = classes;
    }

    // ─── Helper methods for the Many-to-Many relation ───────────
    public void addClass(Classe classe) {
        if (!this.classes.contains(classe)) {
            this.classes.add(classe);
        }
    }

    public void removeClass(Classe classe) {
        this.classes.remove(classe);
    }

    // ─── toString ────────────────────────────────────────────────
    @Override
    public String toString() {
        return nom;
    }

    public String displayMatiere() {
        return "Matiere{id=" + id + ", nom='" + nom + "', coefficient=" + coefficient +
                ", chargehoraire=" + chargehoraire + ", scorecomplexite=" + scorecomplexite +
                ", description='" + description + "', classes=" + classes.size() + "}";
    }
}