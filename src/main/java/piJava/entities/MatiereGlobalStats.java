package piJava.entities;

public class MatiereGlobalStats {
    private final int totalMatieres;
    private final double coefficientMoyen;
    private final double chargeHoraireMoyenne;
    private final double complexiteMoyenne;
    private final double poidsMoyen;
    private final double coefficientMax;
    private final double coefficientMin;
    private final int chargeHoraireMax;
    private final int chargeHoraireMin;
    private final int complexiteMax;
    private final int complexiteMin;
    private final int totalLiensClasseMatiere;
    private final int matieresSansClasse;

    public MatiereGlobalStats(
            int totalMatieres,
            double coefficientMoyen,
            double chargeHoraireMoyenne,
            double complexiteMoyenne,
            double poidsMoyen,
            double coefficientMax,
            double coefficientMin,
            int chargeHoraireMax,
            int chargeHoraireMin,
            int complexiteMax,
            int complexiteMin,
            int totalLiensClasseMatiere,
            int matieresSansClasse
    ) {
        this.totalMatieres = totalMatieres;
        this.coefficientMoyen = coefficientMoyen;
        this.chargeHoraireMoyenne = chargeHoraireMoyenne;
        this.complexiteMoyenne = complexiteMoyenne;
        this.poidsMoyen = poidsMoyen;
        this.coefficientMax = coefficientMax;
        this.coefficientMin = coefficientMin;
        this.chargeHoraireMax = chargeHoraireMax;
        this.chargeHoraireMin = chargeHoraireMin;
        this.complexiteMax = complexiteMax;
        this.complexiteMin = complexiteMin;
        this.totalLiensClasseMatiere = totalLiensClasseMatiere;
        this.matieresSansClasse = matieresSansClasse;
    }

    public int getTotalMatieres() {
        return totalMatieres;
    }

    public double getCoefficientMoyen() {
        return coefficientMoyen;
    }

    public double getChargeHoraireMoyenne() {
        return chargeHoraireMoyenne;
    }

    public double getComplexiteMoyenne() {
        return complexiteMoyenne;
    }

    public double getPoidsMoyen() {
        return poidsMoyen;
    }

    public double getCoefficientMax() {
        return coefficientMax;
    }

    public double getCoefficientMin() {
        return coefficientMin;
    }

    public int getChargeHoraireMax() {
        return chargeHoraireMax;
    }

    public int getChargeHoraireMin() {
        return chargeHoraireMin;
    }

    public int getComplexiteMax() {
        return complexiteMax;
    }

    public int getComplexiteMin() {
        return complexiteMin;
    }

    public int getTotalLiensClasseMatiere() {
        return totalLiensClasseMatiere;
    }

    public int getMatieresSansClasse() {
        return matieresSansClasse;
    }
}

