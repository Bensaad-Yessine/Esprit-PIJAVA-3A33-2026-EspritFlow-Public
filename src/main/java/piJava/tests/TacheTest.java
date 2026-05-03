package piJava.tests;

import piJava.entities.Notification;
import piJava.entities.tache;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import piJava.entities.user;
import piJava.services.NotificationService;
import piJava.services.TacheService;
import piJava.services.UserServices;
import piJava.services.api.*;

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
        String testemail = "tahayassine.snoussi@esprit.tn";

        String html = """
                        <html>
                        <body style="font-family:Arial;background:#f8f9fa;padding:20px;">
                          <p>Test test</p>
                        </body>
                        </html>
                        """;
        ns.sendEmail(testemail,"test Brevo mailing",html);

    }

    @Test
    public void testApiKey() {
        NotifsService ns = new  NotifsService();
        System.out.println(ns.apikey());
    }

    @Test
    public void testProgressData() {
        System.out.println("========== TESTING PROGRESS DATA ==========");

        TacheService ts = new TacheService();
        StatisticsService st = new StatisticsService();
        int userId = 2;
        int days = 30;

        // Get the data
        Map<String, Map<String, Integer>> data = st.getProgressLastDays(userId, days);

        System.out.println("\n--- Date Range Initialized ---");
        System.out.println("Total days in map: " + data.size());
        System.out.println("Expected days: " + days);
        System.out.println("Date range: " +
                LocalDate.now().minusDays(days - 1) + " to " + LocalDate.now());

        System.out.println("\n--- Daily Stats ---");
        int totalCreated = 0;
        int totalCompleted = 0;
        int totalAbandoned = 0;
        int daysWithData = 0;

        for (Map.Entry<String, Map<String, Integer>> entry : data.entrySet()) {
            String date = entry.getKey();
            Map<String, Integer> dayStats = entry.getValue();

            int created = dayStats.getOrDefault("created", 0);
            int completed = dayStats.getOrDefault("completed", 0);
            int abandoned = dayStats.getOrDefault("abandoned", 0);

            // Only print days that have activity
            if (created > 0 || completed > 0 || abandoned > 0) {
                System.out.println(date + " -> Created: " + created +
                        ", Completed: " + completed +
                        ", Abandoned: " + abandoned);
                daysWithData++;
            }

            totalCreated += created;
            totalCompleted += completed;
            totalAbandoned += abandoned;
        }

        System.out.println("\n--- Summary ---");
        System.out.println("Days with activity: " + daysWithData + " out of " + days);
        System.out.println("Total Created: " + totalCreated);
        System.out.println("Total Completed: " + totalCompleted);
        System.out.println("Total Abandoned: " + totalAbandoned);

        // Test with actual tasks to see what dates they have
        System.out.println("\n========== ANALYZING ALL TASKS ==========");
        try {
            List<tache> allTasks = ts.showUserTasks(userId);
            System.out.println("Total tasks for user: " + allTasks.size());

            // Separate old and new tasks
            List<tache> oldTasks = new ArrayList<>();
            List<tache> newTasks = new ArrayList<>();

            for (tache task : allTasks) {
                if (task.getCreated_at() == null && task.getUpdated_at() == null) {
                    oldTasks.add(task);
                } else {
                    newTasks.add(task);
                }
            }

            System.out.println("\n--- OLD TASKS (createdAt/updatedAt are NULL) ---");
            System.out.println("Count: " + oldTasks.size());

            System.out.println("\n--- NEW TASKS (have createdAt/updatedAt) ---");
            System.out.println("Count: " + newTasks.size());
            System.out.println("Showing ALL new tasks:");

            for (int i = 0; i < newTasks.size(); i++) {
                tache task = newTasks.get(i);
                System.out.println("\n=== Task ID: " + task.getId() + " | Status: " + task.getStatut() + " ===");
                System.out.println("  Raw created_at: " + task.getCreated_at());
                System.out.println("  Raw updated_at: " + task.getUpdated_at());

                // Try different conversion methods
                if (task.getCreated_at() != null) {
                    try {
                        // Method 1: toInstant() approach
                        LocalDate method1 = task.getCreated_at().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        System.out.println("  Created (Method 1 - toInstant): " + method1);
                        System.out.println("    In 30-day range? " + data.containsKey(method1.toString()));
                    } catch (Exception e) {
                        System.out.println("  Created (Method 1) FAILED: " + e.getMessage());
                    }

                    try {
                        // Method 2: SimpleDateFormat approach
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        String dateStr = sdf.format(task.getCreated_at());
                        System.out.println("  Created (Method 2 - SimpleDateFormat): " + dateStr);
                        System.out.println("    In 30-day range? " + data.containsKey(dateStr));
                    } catch (Exception e) {
                        System.out.println("  Created (Method 2) FAILED: " + e.getMessage());
                    }
                }

                if (task.getUpdated_at() != null) {
                    try {
                        // Method 1: toInstant() approach
                        LocalDate method1 = task.getUpdated_at().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        System.out.println("  Updated (Method 1 - toInstant): " + method1);
                        System.out.println("    In 30-day range? " + data.containsKey(method1.toString()));
                    } catch (Exception e) {
                        System.out.println("  Updated (Method 1) FAILED: " + e.getMessage());
                    }
                }
            }

            System.out.println("\n--- STATISTICS BREAKDOWN ---");
            System.out.println("Old tasks (NULL createdAt/updatedAt): " + oldTasks.size());
            System.out.println("New tasks (have createdAt/updatedAt): " + newTasks.size());

            // Count statuses in new tasks
            long newTermine = newTasks.stream().filter(t -> "TERMINE".equals(t.getStatut())).count();
            long newAbandon = newTasks.stream().filter(t -> "ABANDON".equals(t.getStatut())).count();
            long newEnCours = newTasks.stream().filter(t -> "EN_COURS".equals(t.getStatut())).count();

            System.out.println("\nNew tasks by status:");
            System.out.println("  TERMINE: " + newTermine);
            System.out.println("  ABANDON: " + newAbandon);
            System.out.println("  EN_COURS: " + newEnCours);
            System.out.println("  Others: " + (newTasks.size() - newTermine - newAbandon - newEnCours));

            // CRITICAL: Check if your database is actually populating these fields
            System.out.println("\n--- DATABASE CHECK ---");
            System.out.println("Are created_at/updated_at being saved to DB?");
            System.out.println("Check your TacheService.add() or update() methods!");
            System.out.println("Do they include created_at and updated_at in the INSERT/UPDATE SQL?");

        } catch (SQLException e) {
            System.out.println("Error fetching tasks: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========== TEST COMPLETE ==========");
    }

    @Test
    public void testFlaskAi () {
        TaskPredictionService aiService = new TaskPredictionService();
        tache t = this.createValidTask();
        TaskPredictionService.TaskPredictionResponse response = aiService.predictTaskCompletion((t));
        System.out.println( response.getProbability_complete()+ "\n"+response.getProbability_abandon());
    }
}