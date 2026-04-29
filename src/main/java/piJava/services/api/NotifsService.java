package piJava.services.api;

import io.github.cdimascio.dotenv.Dotenv;
import piJava.entities.Notification;
import piJava.entities.tache;
import piJava.entities.user;
import piJava.entities.preferenceAlerte;
import piJava.services.NotificationService;
import piJava.services.TacheService;
import piJava.services.AlerteService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class NotifsService {

    private TacheService ts = new TacheService();
    private AlerteService as = new AlerteService();
    private NotificationService ns = new NotificationService();

    Dotenv dotenv = Dotenv.load();
    private String BREVO_API_KEY = dotenv.get("BREVO_API_KEY");

    public void runForUser(user user) {
        try {
            int userId = user.getId();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minusDays(1);

            List<tache> taches = ts.showUserTasks(userId);
            List<String> active = List.of("A_FAIRE", "EN_COURS", "PAUSED");

            List<tache> activetasks = taches.stream()
                    .filter(t -> active.contains(t.getStatut()))
                    .toList();

            if (activetasks.isEmpty()) return;

            List<Notification> Notifs = ns.showUserNotifs(userId);
            List<Notification> recentNotifs = Notifs.stream()
                    .filter(n -> n.getCreatedAt().isAfter(oneDayAgo))
                    .toList();

            Map<Integer, Map<String, Map<String, Boolean>>> index = new HashMap<>();

            for (Notification notif : recentNotifs) {
                if (notif.getTacheId() == null) continue;

                int taskId = notif.getTacheId();
                String type = notif.getType();
                String channel = notif.isEmail() ? "email" : "site";

                index.computeIfAbsent(taskId, k -> new HashMap<>())
                        .computeIfAbsent(type, k -> new HashMap<>())
                        .put(channel, true);
            }

            for (tache t : activetasks) {

                if (t.getDate_fin() == null) continue;

                LocalDateTime deadline = t.getDate_fin()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                long remainingTime = Duration.between(now, deadline).getSeconds();

                if (remainingTime <= 3600 && remainingTime > 0) {
                    handleAlert(user, t, "INFO", index);
                } else if (remainingTime <= 0) {
                    handleAlert(user, t, "WARNING", index);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur " + e.getMessage());
        }
    }

    private String formatPriority(String priority) {
        return switch (priority) {
            case "ELEVEE" -> "Haute";
            case "MOYENNE" -> "Moyenne";
            case "FAIBLE" -> "Basse";
            default -> priority;
        };
    }

    private void handleAlert(user user, tache t, String type,
                             Map<Integer, Map<String, Map<String, Boolean>>> index) {

        int userId = user.getId();
        int taskId = t.getId();

        try {
            preferenceAlerte preference = as.showUserAlertes(userId).stream()
                    .filter(p -> p.getIs_active() != null && p.getIs_active())
                    .findFirst()
                    .orElse(null);

            boolean emailActive = preference == null || Boolean.TRUE.equals(preference.getEmail_actif());
            boolean siteNotifActive = preference == null || Boolean.TRUE.equals(preference.getSite_notif_active());

            String taskTitle = t.getTitre() != null ? t.getTitre() : "Tâche sans titre";
            String priority = formatPriority(t.getPriorite() != null ? t.getPriorite() : "MOYENNE");

            LocalDateTime deadlineDate = t.getDate_fin()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String deadline = deadlineDate.format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );

            String message = type.equals("INFO")
                    ? "⏰ Rappel : \"" + taskTitle + "\" expire bientôt !"
                    : "⚠️ En retard : \"" + taskTitle + "\" est en retard !";

            boolean existsite = index.containsKey(taskId)
                    && index.get(taskId).containsKey(type)
                    && index.get(taskId).get(type).containsKey("site");

            boolean existemail = index.containsKey(taskId)
                    && index.get(taskId).containsKey(type)
                    && index.get(taskId).get(type).containsKey("email");

            if (siteNotifActive && !existsite) {
                Notification n = new Notification();
                n.setTacheId(taskId);
                n.setType(type);
                n.setEmail(false);
                n.setUserId(userId);
                n.setCreatedAt(LocalDateTime.now());
                n.setMessage(message);
                n.setRead(false);
                ns.add(n);
            }

            if (emailActive && !existemail) {
                System.out.println("📧 EMAIL BLOCK ENTERED for task " + taskId);
                String emailSubject = type.equals("INFO")
                        ? "⏰ Rappel tâche proche échéance"
                        : "⚠️ Tâche en retard";

                String html = """
                        <html>
                        <body style="font-family:Arial;background:#f8f9fa;padding:20px;">
                          <div style="max-width:600px;margin:auto;background:white;border:2px solid #dc3545;border-radius:10px;overflow:hidden;">
                            
                            <div style="background:#dc3545;color:white;padding:15px;text-align:center;font-size:18px;">
                              Task Notification
                            </div>

                            <div style="padding:20px;">
                              <p>%s</p>

                              <div style="border-left:5px solid #dc3545;padding:10px;background:#fff5f5;">
                                <p><b>Title:</b> %s</p>
                                <p><b>Priority:</b> %s</p>
                                <p><b>Deadline:</b> %s</p>
                              </div>
                            </div>

                          </div>
                        </body>
                        </html>
                        """.formatted(message, taskTitle, priority, deadline);

                sendEmail(user.getEmail(), emailSubject, html);

                Notification n = new Notification();
                n.setTacheId(taskId);
                n.setType(type);
                n.setEmail(true);
                n.setUserId(userId);
                n.setCreatedAt(LocalDateTime.now());
                n.setMessage(message);
                n.setRead(false);
                ns.add(n);
            }

        } catch (SQLException e) {
            System.out.println("Erreur " + e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, String htmlBody) {

        try {

            String safeHtml = htmlBody
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "");

            String json = "{"
                    + "\"sender\":{"
                    + "\"name\":\"Task App\","
                    + "\"email\":\"tahayassinesnoussi05@gmail.com\""
                    + "},"
                    + "\"to\":[{"
                    + "\"email\":\"" + to + "\""
                    + "}],"
                    + "\"subject\":\"" + subject + "\","
                    + "\"htmlContent\":\"" + safeHtml + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", BREVO_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("EMAIL STATUS: " + response.statusCode());
            System.out.println("EMAIL RESPONSE: " + response.body());

        } catch (Exception e) {
            System.out.println("EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Notification> getUserNotifications(int userId) {
        try {
            List<Notification> notifs = ns.showUserNotifs(userId);

            // Sort newest first
            notifs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            Map<Integer, Notification> latestPerTask = new HashMap<>();
            List<Notification> general = new ArrayList<>();

            for (Notification n : notifs) {

                Integer taskId = n.getTacheId();

                if (taskId != null) {
                    // keep only latest per task
                    if (!latestPerTask.containsKey(taskId)) {
                        latestPerTask.put(taskId, n);
                    }
                } else {
                    // notifications without task
                    general.add(n);
                }
            }

            // merge both
            List<Notification> result = new ArrayList<>(latestPerTask.values());
            result.addAll(general);

            // final sort again (important!)
            result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            return result;

        } catch (SQLException e) {
            System.out.println("Erreur getUserNotifications: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}