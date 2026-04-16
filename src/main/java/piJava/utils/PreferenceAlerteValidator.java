package piJava.utils;

import piJava.entities.preferenceAlerte;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PreferenceAlerteValidator {

    // ── Full object validation ─────────────────────────────────
    public static List<String> validate(preferenceAlerte alerte) {
        List<String> errors = new ArrayList<>();

        errors.addAll(validateNom(alerte.getNom()));
        errors.addAll(validateDescription(alerte.getDescription()));
        errors.addAll(validateDelai(alerte.getDelai_rappel_min()));

        if (alerte.getHeure_silence_debut() != null) {
            errors.addAll(validateHeure(alerte.getHeure_silence_debut()));
        }
        if (alerte.getHeure_silence_fin() != null) {
            errors.addAll(validateHeure(alerte.getHeure_silence_fin()));
        }
        if (alerte.getHeure_silence_debut() != null && alerte.getHeure_silence_fin() != null) {
            if (alerte.getHeure_silence_debut().equals(alerte.getHeure_silence_fin())) {
                errors.add("L'heure de début et de fin doivent être différentes.");
            }
        }

        return errors;
    }

    // ── Field-level validators ─────────────────────────────────
    public static List<String> validateNom(String nom) {
        List<String> errors = new ArrayList<>();
        if (nom == null || nom.trim().isEmpty()) {
            errors.add("Le nom est obligatoire.");
            return errors;
        }
        if (nom.trim().length() < 3) {
            errors.add("Le nom doit contenir au moins 3 caractères.");
        }
        if (nom.trim().length() > 100) {
            errors.add("Le nom ne doit pas dépasser 100 caractères.");
        }
        return errors;
    }

    public static List<String> validateDescription(String description) {
        List<String> errors = new ArrayList<>();
        if (description == null || description.trim().isEmpty()) {
            return errors; // Description is optional
        }
        if (description.trim().length() < 10) {
            errors.add("La description doit contenir au moins 10 caractères.");
        }
        if (description.trim().length() > 500) {
            errors.add("La description ne doit pas dépasser 500 caractères.");
        }
        return errors;
    }

    public static List<String> validateDelai(int delai) {
        List<String> errors = new ArrayList<>();
        if (delai < 0) {
            errors.add("Le délai ne peut pas être négatif.");
        }
        if (delai > 10080) { // max 1 week in minutes
            errors.add("Le délai ne peut pas dépasser 10080 minutes (1 semaine).");
        }
        return errors;
    }

    public static List<String> validateDelaiText(String delaiText) {
        List<String> errors = new ArrayList<>();
        if (delaiText == null || delaiText.trim().isEmpty()) {
            errors.add("Le délai est obligatoire.");
            return errors;
        }
        try {
            int delai = Integer.parseInt(delaiText.trim());
            errors.addAll(validateDelai(delai));
        } catch (NumberFormatException e) {
            errors.add("Le délai doit être un nombre entier.");
        }
        return errors;
    }

    public static List<String> validateHeure(LocalTime heure) {
        List<String> errors = new ArrayList<>();
        if (heure == null) {
            errors.add("L'heure est invalide.");
        }
        return errors;
    }

    public static List<String> validateHeureText(String heureText) {
        List<String> errors = new ArrayList<>();
        if (heureText == null || heureText.trim().isEmpty()) {
            return errors; // Optional field
        }
        try {
            LocalTime.parse(heureText.trim());
        } catch (Exception e) {
            errors.add("Format d'heure invalide. Utilisez HH:mm (ex: 08:30).");
        }
        return errors;
    }
}