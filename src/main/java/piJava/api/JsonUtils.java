package piJava.api;

import piJava.entities.ClasseStats;
import piJava.entities.MatiereGlobalStats;

import java.util.List;

public final class JsonUtils {

    private JsonUtils() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        sb.append('"');
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String quote(String value) {
        return value == null ? "null" : escape(value);
    }

    public static String classeStatsToJson(ClasseStats stats) {
        if (stats == null) {
            return "null";
        }
        return "{" +
                "\"classeId\":" + stats.getClasseId() + ',' +
                "\"nom\":" + quote(stats.getNom()) + ',' +
                "\"niveau\":" + quote(stats.getNiveau()) + ',' +
                "\"filiere\":" + quote(stats.getFiliere()) + ',' +
                "\"anneeUniversitaire\":" + quote(stats.getAnneeUniversitaire()) + ',' +
                "\"nombreMatieres\":" + stats.getNombreMatieres() + ',' +
                "\"totalCoefficient\":" + stats.getTotalCoefficient() + ',' +
                "\"totalChargeHoraire\":" + stats.getTotalChargeHoraire() + ',' +
                "\"moyenneComplexite\":" + stats.getMoyenneComplexite() +
                '}';
    }

    public static String classeStatsListToJson(List<ClasseStats> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (stats != null) {
            for (int i = 0; i < stats.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(classeStatsToJson(stats.get(i)));
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public static String matiereGlobalStatsToJson(MatiereGlobalStats stats) {
        if (stats == null) {
            return "null";
        }
        return "{" +
                "\"totalMatieres\":" + stats.getTotalMatieres() + ',' +
                "\"coefficientMoyen\":" + stats.getCoefficientMoyen() + ',' +
                "\"chargeHoraireMoyenne\":" + stats.getChargeHoraireMoyenne() + ',' +
                "\"complexiteMoyenne\":" + stats.getComplexiteMoyenne() + ',' +
                "\"poidsMoyen\":" + stats.getPoidsMoyen() + ',' +
                "\"coefficientMax\":" + stats.getCoefficientMax() + ',' +
                "\"coefficientMin\":" + stats.getCoefficientMin() + ',' +
                "\"chargeHoraireMax\":" + stats.getChargeHoraireMax() + ',' +
                "\"chargeHoraireMin\":" + stats.getChargeHoraireMin() + ',' +
                "\"complexiteMax\":" + stats.getComplexiteMax() + ',' +
                "\"complexiteMin\":" + stats.getComplexiteMin() + ',' +
                "\"totalLiensClasseMatiere\":" + stats.getTotalLiensClasseMatiere() + ',' +
                "\"matieresSansClasse\":" + stats.getMatieresSansClasse() +
                '}';
    }

    public static String errorJson(String message) {
        return "{\"error\":" + quote(message) + "}";
    }

    public static String healthJson() {
        return "{\"status\":\"ok\"}";
    }
}

