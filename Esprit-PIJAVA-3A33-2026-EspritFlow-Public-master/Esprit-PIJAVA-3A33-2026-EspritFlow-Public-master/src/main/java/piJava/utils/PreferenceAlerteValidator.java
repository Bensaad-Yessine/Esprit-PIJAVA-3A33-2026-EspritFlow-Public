package piJava.utils;

import piJava.entities.preferenceAlerte;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PreferenceAlerteValidator {

    // ═══════════════════════════════════════════════
    // FULL VALIDATION
    // ═══════════════════════════════════════════════
    public static List<String> validate(preferenceAlerte p) {
        List<String> errors = new ArrayList<>();

        errors.addAll(validateNom(p.getNom()));
        errors.addAll(validateDescription(p.getDescription()));
        errors.addAll(validateDelaiRappel(p.getDelai_rappel_min()));
        errors.addAll(validateSilenceTimes(p.getHeure_silence_debut(), p.getHeure_silence_fin()));
        errors.addAll(validateUser(p.getUser_id())); // adapt if needed

        return errors;
    }

    // ═══════════════════════════════════════════════
    // NOM (TITLE)
    // ═══════════════════════════════════════════════
    public static List<String> validateNom(String nom) {
        List<String> errors = new ArrayList<>();

        if (nom == null || nom.trim().isEmpty()) {
            errors.add("Le nom de la préférence est obligatoire.");
        } else {
            if (nom.length() < 5) {
                errors.add("Le nom doit contenir au moins 5 caractères.");
            }

            if (nom.length() > 50) {
                errors.add("Le nom ne doit pas dépasser 50 caractères.");
            }

            if (!Character.isUpperCase(nom.charAt(0))) {
                errors.add("Le nom doit commencer par une majuscule.");
            }
        }

        return errors;
    }

    // ═══════════════════════════════════════════════
    // DESCRIPTION (YOU REQUESTED ADDITION)
    // ═══════════════════════════════════════════════
    public static List<String> validateDescription(String description) {
        List<String> errors = new ArrayList<>();

        if (description == null || description.trim().isEmpty()) {
            errors.add("La description est obligatoire.");
        } else {
            if (description.length() < 10) {
                errors.add("La description doit contenir au moins 10 caractères.");
            }

            if (!Character.isUpperCase(description.charAt(0))) {
                errors.add("La description doit commencer par une majuscule.");
            }
        }

        return errors;
    }

    // ═══════════════════════════════════════════════
    // DELAI RAPPEL
    // ═══════════════════════════════════════════════
    public static List<String> validateDelaiRappel(int delai) {
        List<String> errors = new ArrayList<>();

        if (delai <= 0) {
            errors.add("Le délai de rappel doit être positif.");
        }

        return errors;
    }

    // ═══════════════════════════════════════════════
    // USER
    // ═══════════════════════════════════════════════
    public static List<String> validateUser(int userId) {
        List<String> errors = new ArrayList<>();

        if (userId == 0) {
            errors.add("L'utilisateur est obligatoire.");
        }

        return errors;
    }

    // ═══════════════════════════════════════════════
    // SILENCE TIMES
    // ═══════════════════════════════════════════════
    public static List<String> validateSilenceTimes(
            LocalTime debut,
            LocalTime fin
    ) {
        List<String> errors = new ArrayList<>();

        if (debut == null) {
            errors.add("L'heure de début du silence est obligatoire.");
        }

        if (fin == null) {
            errors.add("L'heure de fin du silence est obligatoire.");
        }

        if (debut != null && fin != null) {

            // ONLY rule: must be different
            if (debut.equals(fin)) {
                errors.add("L’heure de début et de fin doivent être différentes.");
            }

            // NO ordering rule (start can be after end)
        }

        return errors;
    }

    // ═══════════════════════════════════════════════
    // DELAI RAPPEL TEXT
    // ═══════════════════════════════════════════════
    public static List<String> validateDelaiText(String delaiText) {
        List<String> errors = new ArrayList<>();

        if (delaiText != null && !delaiText.trim().isEmpty()) {
            try {
                int delai = Integer.parseInt(delaiText);
                errors.addAll(validateDelaiRappel(delai));
            } catch (NumberFormatException e) {
                errors.add("Le délai doit être un nombre entier.");
            }
        } else {
            errors.add("Le délai est obligatoire.");
        }

        return errors;
    }

    // ═══════════════════════════════════════════════
    // HEURE TEXT
    // ═══════════════════════════════════════════════
    public static List<String> validateHeureText(String heureText) {
        List<String> errors = new ArrayList<>();

        if (heureText != null && !heureText.trim().isEmpty()) {
            try {
                LocalTime.parse(heureText);
            } catch (Exception e) {
                errors.add("L'heure doit être au format HH:mm.");
            }
        } else {
            errors.add("L'heure est obligatoire.");
        }

        return errors;
    }
}