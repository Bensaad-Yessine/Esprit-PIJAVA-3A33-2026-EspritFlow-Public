package piJava.tests;

import piJava.entities.preferenceAlerte;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalTime;
import java.util.List;

public class AlerteTest {


    private preferenceAlerte alerte1 =  new preferenceAlerte();
    private preferenceAlerte alerte2 =  new preferenceAlerte();

    private preferenceAlerte createValidAlerte() {

        preferenceAlerte alerte = new preferenceAlerte();
        alerte.setUser_id(1);
        alerte.setIs_active(false);
        alerte.setNom("Alerte Travail");
        alerte.setDescription("Description de l'alerte valide");
        alerte.setDelai_rappel_min(50);
        alerte.setEmail_actif(true);
        alerte.setIs_default(false);
        alerte.setHeure_silence_debut(LocalTime.of(22, 0));
        alerte.setHeure_silence_fin(LocalTime.of(7, 0));

        return alerte;
    }

    @Test
    public void invalidNomAndDescriptionTest() {

        // alerte1: short
        preferenceAlerte alerte1 = createValidAlerte();
        alerte1.setNom("Abc");
        alerte1.setDescription("Short");
        List<String> errors1 = piJava.utils.PreferenceAlerteValidator.validate(alerte1);

        // ALERT 2: long but lowercase start
        preferenceAlerte alerte2 = createValidAlerte();
        alerte2.setNom("alerte travail super long name");
        alerte2.setDescription("description valide");

        List<String> errors2 =
                piJava.utils.PreferenceAlerteValidator.validate(alerte2);

        // tests
        assertTrue(errors1.contains("Le nom doit contenir au moins 5 caractères."));
        assertTrue(errors1.contains("La description doit contenir au moins 10 caractères."));

        assertTrue(errors2.contains("Le nom doit commencer par une majuscule."));
        assertTrue(errors2.contains("La description doit commencer par une majuscule."));
    }

    @Test
    public void validAlerteTest() {

        preferenceAlerte alerte = createValidAlerte();

        java.util.List<String> errors =
                piJava.utils.PreferenceAlerteValidator.validate(alerte);
        assertTrue(errors.isEmpty());
    }
}