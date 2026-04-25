package piJava.entities;

public class ClasseStats {
    private final int classeId;
    private final String nom;
    private final String niveau;
    private final String filiere;
    private final String anneeUniversitaire;
    private final int nombreMatieres;
    private final double totalCoefficient;
    private final int totalChargeHoraire;
    private final double moyenneComplexite;

    public ClasseStats(
            int classeId,
            String nom,
            String niveau,
            String filiere,
            String anneeUniversitaire,
            int nombreMatieres,
            double totalCoefficient,
            int totalChargeHoraire,
            double moyenneComplexite
    ) {
        this.classeId = classeId;
        this.nom = nom;
        this.niveau = niveau;
        this.filiere = filiere;
        this.anneeUniversitaire = anneeUniversitaire;
        this.nombreMatieres = nombreMatieres;
        this.totalCoefficient = totalCoefficient;
        this.totalChargeHoraire = totalChargeHoraire;
        this.moyenneComplexite = moyenneComplexite;
    }

    public int getClasseId() {
        return classeId;
    }

    public String getNom() {
        return nom;
    }

    public String getNiveau() {
        return niveau;
    }

    public String getFiliere() {
        return filiere;
    }

    public String getAnneeUniversitaire() {
        return anneeUniversitaire;
    }

    public int getNombreMatieres() {
        return nombreMatieres;
    }

    public double getTotalCoefficient() {
        return totalCoefficient;
    }

    public int getTotalChargeHoraire() {
        return totalChargeHoraire;
    }

    public double getMoyenneComplexite() {
        return moyenneComplexite;
    }
}

