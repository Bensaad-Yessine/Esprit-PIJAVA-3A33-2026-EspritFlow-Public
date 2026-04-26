package piJava.tests;

import piJava.entities.Notification;
import piJava.entities.tache;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import piJava.entities.user;
import piJava.services.NotificationService;
import piJava.services.UserServices;
import piJava.services.api.BehaviorAnalysisService;
import piJava.services.api.NotifsService;
import piJava.services.api.WeatherAiService;
public class TacheTest {

    private tache task;
    private tache task2;

    private tache createValidTask() {
        long now = System.currentTimeMillis();

        tache t = new tache();
        t.setTitre("Titre 1");
        t.setDescription("Description 1");
        t.setType("MANUEL");
        t.setPriorite("ELEVEE");
        t.setStatut("A_FAIRE");
        t.setUser_id(1);
        t.setDate_debut(new Date(now + 60 * 60 * 1000));
        t.setDate_fin(new Date(now + 3 * 60 * 60 * 1000));
        t.setDuree_estimee(2);

        return t;
    }

    @Test
    public void invalidDatesTest() {
        // your tests here

        task = createValidTask();
        long now = System.currentTimeMillis();
        // errors must be negative duree estimee +date debut in the past + date fin before date debut
        task.setDate_debut(new Date(now - 60 * 60 * 1000));
        task.setDate_fin(new Date(now - 3 * 60 * 60 * 1000));
        task.setDuree_estimee(-40);


        List<String> errors = piJava.utils.TacheValidator.validate(task);
        assertTrue(errors.contains("La date de début doit être dans le futur."));
        assertTrue(errors.contains("La date de fin doit être après la date de début."));
        assertTrue(errors.contains("La durée estimée doit être positive."));


    }

    @Test
    public void invalidStringsTest() {
        // task title and desc starts with lower cases
        task = createValidTask();
        task.setTitre("titre 1");
        task.setDescription("description 1");
        List<String> errors = piJava.utils.TacheValidator.validate(task);
        // task2 titre is empty
        task2=createValidTask();
        task2.setTitre("");
        List<String> errors2 = piJava.utils.TacheValidator.validate(task2);

        //tests
        assertTrue(errors.contains("Le titre doit commencer par une majuscule."));
        assertTrue(errors.contains("La description doit commencer par une majuscule."));

        assertTrue(errors2.contains("Le titre ne peut pas être vide."));
    }

    @Test
    public void validtest() {
        task = createValidTask();
        List<String> errors = piJava.utils.TacheValidator.validate(task);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void testBehaviour() {
        BehaviorAnalysisService s = new BehaviorAnalysisService();

        System.out.println(s.getOrComputeProfile(1));
    }

    @Test
    public void testNotifs(){
        NotifsService ns = new NotifsService();
        UserServices us = new UserServices();
        NotificationService notifService = new NotificationService();
        user u = us.getById(1);

        ns.runForUser(u);

        List<Notification> notifs = null;
        try {
            notifs = notifService.showUserNotifs(u.getId());
        } catch (SQLException e) {
            System.out.println("Erreur " + e.getMessage());
        }

        for (Notification n : notifs) {
            System.out.println(
                    n.getId() + " | " +
                            n.getMessage() + " | " +
                            n.getType() + " | " +
                            n.isEmail() + " | " +
                            n.getCreatedAt()
            );
        }
    }

    @Test
    public void testBrevo() {
        NotifsService ns = new  NotifsService();
        String testemail = "tahayassinesnoussi05@gmail.com";

        String html = """
                        <html>
                        <body style="font-family:Arial;background:#f8f9fa;padding:20px;">
                          <p>Test test</p>
                        </body>
                        </html>
                        """;
        ns.sendEmail(testemail,"test Brevo mailing",html);

    }

}