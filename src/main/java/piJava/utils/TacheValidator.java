package piJava.utils;

import piJava.entities.tache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TacheValidator {

    public static List<String> validate(tache t) {
        List<String> errors = new ArrayList<>();

        errors.addAll(validateTitre(t.getTitre()));
        errors.addAll(validateDescription(t.getDescription()));
        errors.addAll(validateType(t.getType()));
        errors.addAll(validatePriorite(t.getPriorite()));
        errors.addAll(validateStatut(t.getStatut()));
        errors.addAll(validateUser(t.getUser_id()));
        errors.addAll(validateDates(t.getDate_debut(), t.getDate_fin()));
        errors.addAll(validateDureeEstimee(t.getDuree_estimee()));

        return errors;
    }

    // ═══════════════════════════════════════════════════════════
    // INDIVIDUAL FIELD VALIDATORS (for real-time validation)
    // ═══════════════════════════════════════════════════════════

    public static List<String> validateTitre(String titre) {
        List<String> errors = new ArrayList<>();

        if (titre == null || titre.trim().isEmpty()) {
            errors.add("Le titre ne peut pas être vide.");
        } else {
            if (titre.length() > 100) {
                errors.add("Le titre ne peut pas dépasser 100 caractères.");
            }

            if (!Character.isUpperCase(titre.charAt(0))) {
                errors.add("Le titre doit commencer par une majuscule.");
            }
        }

        return errors;
    }

    public static List<String> validateDescription(String description) {
        List<String> errors = new ArrayList<>();

        if (description == null || description.trim().isEmpty()) {
            errors.add("La description est obligatoire.");
        } else {
            if (!Character.isUpperCase(description.charAt(0))) {
                errors.add("La description doit commencer par une majuscule.");
            }
        }

        return errors;
    }

    public static List<String> validateType(String type) {
        List<String> errors = new ArrayList<>();

        if (type == null) {
            errors.add("Le type est obligatoire.");
        } else if (!List.of("MANUEL","EMPLOI","REUNION","SANTE","REVISION").contains(type)) {
            errors.add("Type invalide.");
        }

        return errors;
    }

    public static List<String> validatePriorite(String priorite) {
        List<String> errors = new ArrayList<>();

        if (priorite == null) {
            errors.add("La priorité est obligatoire.");
        } else if (!List.of("ELEVEE", "FAIBLE", "MOYEN").contains(priorite)) {
            errors.add("Type invalide.");
        }

        return errors;
    }

    public static List<String> validateStatut(String statut) {
        List<String> errors = new ArrayList<>();

        if (statut == null) {
            errors.add("Le statut est obligatoire.");
        }else if (!List.of("A_FAIRE", "EN_COURS", "PAUSED","TERMINE","ABANDON").contains(statut)) {
            errors.add("Statut invalide.");
        }

        return errors;
    }

    public static List<String> validateUser(int userId) {
        List<String> errors = new ArrayList<>();

        if (userId == 0) {
            errors.add("L'utilisateur est obligatoire.");
        }

        return errors;
    }

    public static List<String> validateDateDebut(Date dateDebut) {
        List<String> errors = new ArrayList<>();
        Date now = new Date();

        if (dateDebut == null) {
            errors.add("La date de début est obligatoire.");
        } else if (dateDebut.before(now)) {
            errors.add("La date de début doit être dans le futur.");
        }

        return errors;
    }

    public static List<String> validateDateFin(Date dateFin) {
        List<String> errors = new ArrayList<>();

        if (dateFin == null) {
            errors.add("La date de fin est obligatoire.");
        }

        return errors;
    }

    public static List<String> validateDates(Date dateDebut, Date dateFin) {
        List<String> errors = new ArrayList<>();

        // First validate individual dates
        errors.addAll(validateDateDebut(dateDebut));
        errors.addAll(validateDateFin(dateFin));

        // Then validate relationship between dates
        if (dateDebut != null && dateFin != null) {
            // date fin > date debut
            if (dateFin.before(dateDebut)) {
                errors.add("La date de fin doit être après la date de début.");
            }

            // durée <= 6 heures
            long diffMillis = dateFin.getTime() - dateDebut.getTime();
            long maxDuration = 6 * 60 * 60 * 1000L;

            if (diffMillis > maxDuration) {
                errors.add("La tâche ne peut pas durer plus de 6 heures.");
            }
        }

        return errors;
    }

    public static List<String> validateDureeEstimee(int dureeEstimee) {
        List<String> errors = new ArrayList<>();

        if (dureeEstimee != 0) {
            if (dureeEstimee < 0) {
                errors.add("La durée estimée doit être positive.");
            }
        }

        return errors;
    }

    // ═══════════════════════════════════════════════════════════
    // HELPER: Check if text is a valid number
    // ═══════════════════════════════════════════════════════════
    public static List<String> validateDureeEstimeeText(String dureeText) {
        List<String> errors = new ArrayList<>();

        if (dureeText != null && !dureeText.trim().isEmpty()) {
            try {
                int duree = Integer.parseInt(dureeText);
                errors.addAll(validateDureeEstimee(duree));
            } catch (NumberFormatException e) {
                errors.add("La durée doit être un nombre entier.");
            }
        }

        return errors;
    }
}