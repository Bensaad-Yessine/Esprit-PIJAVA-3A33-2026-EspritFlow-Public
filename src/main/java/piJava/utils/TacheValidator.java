package piJava.utils;

import piJava.entities.tache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class TacheValidator {

    private static final List<String> TYPES = Arrays.asList("MANUEL", "REUNION", "REVISION", "SANTE", "EMPLOI");
    private static final List<String> PRIORITES = Arrays.asList("FAIBLE", "MOYEN", "ELEVEE");
    private static final List<String> STATUTS = Arrays.asList("A_FAIRE", "EN_COURS", "TERMINE", "EN_RETARD", "PAUSED", "ABANDON");

    private TacheValidator() {
        // Utility class
    }

    public static List<String> validate(tache task) {
        if (task == null) {
            return Collections.singletonList("Tache invalide.");
        }

        List<String> errors = new ArrayList<>();
        errors.addAll(validateTitre(task.getTitre()));
        errors.addAll(validateDescription(task.getDescription()));
        errors.addAll(validateType(task.getType()));
        errors.addAll(validatePriorite(task.getPriorite()));
        errors.addAll(validateStatut(task.getStatut()));
        errors.addAll(validateDateDebut(task.getDate_debut()));
        errors.addAll(validateDates(task.getDate_debut(), task.getDate_fin()));
        errors.addAll(validateDureeEstimee(task.getDuree_estimee()));
        return errors;
    }

    public static List<String> validateTitre(String titre) {
        List<String> errors = new ArrayList<>();
        if (isBlank(titre)) {
            errors.add("Le titre est obligatoire.");
            return errors;
        }

        String value = titre.trim();
        if (value.length() < 3) {
            errors.add("Le titre doit contenir au moins 3 caracteres.");
        }
        if (value.length() > 120) {
            errors.add("Le titre ne doit pas depasser 120 caracteres.");
        }
        return errors;
    }

    public static List<String> validateDescription(String description) {
        List<String> errors = new ArrayList<>();
        if (description == null || description.trim().isEmpty()) {
            return errors;
        }

        if (description.trim().length() < 5) {
            errors.add("La description doit contenir au moins 5 caracteres.");
        }
        return errors;
    }

    public static List<String> validateType(String type) {
        List<String> errors = new ArrayList<>();
        if (isBlank(type)) {
            errors.add("Le type est obligatoire.");
        } else if (!TYPES.contains(type)) {
            errors.add("Type invalide.");
        }
        return errors;
    }

    public static List<String> validatePriorite(String priorite) {
        List<String> errors = new ArrayList<>();
        if (isBlank(priorite)) {
            errors.add("La priorite est obligatoire.");
        } else if (!PRIORITES.contains(priorite)) {
            errors.add("Priorite invalide.");
        }
        return errors;
    }

    public static List<String> validateStatut(String statut) {
        List<String> errors = new ArrayList<>();
        if (isBlank(statut)) {
            errors.add("Le statut est obligatoire.");
        } else if (!STATUTS.contains(statut)) {
            errors.add("Statut invalide.");
        }
        return errors;
    }

    public static List<String> validateDateDebut(Date dateDebut) {
        List<String> errors = new ArrayList<>();
        if (dateDebut == null) {
            errors.add("La date de debut est obligatoire.");
        }
        return errors;
    }

    public static List<String> validateDates(Date dateDebut, Date dateFin) {
        List<String> errors = new ArrayList<>();
        if (dateFin == null) {
            errors.add("La date de fin est obligatoire.");
            return errors;
        }

        if (dateDebut != null && dateFin.before(dateDebut)) {
            errors.add("La date de fin doit etre apres la date de debut.");
        }
        return errors;
    }

    public static List<String> validateDureeEstimeeText(String dureeText) {
        if (isBlank(dureeText)) {
            return Collections.emptyList();
        }

        try {
            int value = Integer.parseInt(dureeText.trim());
            return validateDureeEstimee(value);
        } catch (NumberFormatException e) {
            return Collections.singletonList("La duree estimee doit etre un entier.");
        }
    }

    public static List<String> validateDureeEstimee(int duree) {
        List<String> errors = new ArrayList<>();
        if (duree < 0) {
            errors.add("La duree estimee ne peut pas etre negative.");
        }
        if (duree > 10080) {
            errors.add("La duree estimee est trop grande.");
        }
        return errors;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

