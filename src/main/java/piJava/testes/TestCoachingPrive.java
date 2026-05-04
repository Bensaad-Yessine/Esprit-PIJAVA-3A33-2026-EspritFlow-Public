package piJava.testes;

import piJava.entities.ObjectifSante;
import piJava.entities.SuiviBienEtre;
import piJava.services.api.CoachingPriveService;
import piJava.services.api.CoachingResponse;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TestCoachingPrive {

    public static void main(String[] args) throws org.json.JSONException {
        ObjectifSante objectif = new ObjectifSante(
                1,
                "Améliorer mon sommeil",
                "SOMMEIL",
                8,
                Date.valueOf("2026-04-01"),
                Date.valueOf("2026-04-30"),
                "HAUTE",
                "EN_COURS",
                2
        );

        List<SuiviBienEtre> suivis = new ArrayList<>();

        suivis.add(new SuiviBienEtre(
                1,
                Date.valueOf("2026-04-20"),
                "Fatigué",
                4,
                5,
                7,
                6,
                "J'ai dormi tard.",
                48,
                1
        ));

        suivis.add(new SuiviBienEtre(
                2,
                Date.valueOf("2026-04-22"),
                "Moyen",
                6,
                6,
                5,
                7,
                "Sommeil un peu meilleur.",
                62,
                1
        ));

        CoachingPriveService service = new CoachingPriveService();
        CoachingResponse response = service.genererCoaching(objectif, suivis);

        System.out.println("Titre : " + response.getTitre());
        System.out.println("Message motivation : " + response.getMessageMotivation());
        System.out.println("Résumé analyse : " + response.getResumeAnalyse());
        System.out.println("Score moyen : " + response.getScoreMoyen());
        System.out.println("Tendance : " + response.getTendance());
        System.out.println("Point fort : " + response.getPointFort());
        System.out.println("Point faible : " + response.getPointFaible());
        System.out.println("Niveau : " + response.getNiveau());

        System.out.println("Message de confiance : " + response.getMessageConfiance());

        System.out.println("Conseils type objectif :");
        for (String conseil : response.getConseilsTypeObjectif()) {
            System.out.println("- " + conseil);
        }

        System.out.println("Conseils niveaux faibles :");
        for (String conseil : response.getConseilsNiveauxFaibles()) {
            System.out.println("- " + conseil);
        }

        System.out.println("Résumé suivis :");
        for (String suivi : response.getResumeSuivis()) {
            System.out.println("- " + suivi);
        }
    }
}