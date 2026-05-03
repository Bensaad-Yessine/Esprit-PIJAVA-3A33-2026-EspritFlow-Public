package piJava.services.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONException;
import org.json.JSONObject;
import piJava.entities.suiviTache;
import piJava.entities.tache;
import piJava.services.StudentIntelligenceProfileService;
import piJava.entities.StudentIntelligenceProfile;
import piJava.services.SuiviTacheService;
import piJava.services.TacheService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BehaviorAnalysisService {

    private StudentIntelligenceProfileService si= new  StudentIntelligenceProfileService();
    private String groqKey;
    public BehaviorAnalysisService() {
        Dotenv dotenv = Dotenv.load();
        this.groqKey = dotenv.get("GROQ_API_KEY");
    }

    public StudentIntelligenceProfile getOrComputeProfile(int userId) {

        try {
            StudentIntelligenceProfile sp = si.showLatestPerUser(userId);

            LocalDateTime now = LocalDateTime.now();

            if (sp == null) {
                return computeAndSaveProfile(userId, null);
            }

            LocalDateTime analyzed = sp.getAnalyzedAt().toLocalDateTime();
            long days = Duration.between(analyzed, now).toDays();

            if (days > 7) {
                return computeAndSaveProfile(userId, analyzed);
            }

            return sp;

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
            return null;
        }
    }

    private StudentIntelligenceProfile computeAndSaveProfile(int userId, LocalDateTime since) {
        try {
            StudentIntelligenceProfile sp = new StudentIntelligenceProfile();
            TacheService ts = new TacheService();
            SuiviTacheService sts = new SuiviTacheService();

            // ✅ FIX: normalize since
            LocalDateTime safeSince = (since == null)
                    ? LocalDateTime.now().minusYears(1)
                    : since;

            List<tache> tasks = ts.getTasksByUserSince(userId, safeSince);
            List<suiviTache> suivis = sts.getSuivisByUserSince(userId, safeSince);

            Map<String, Object> metrics = computeBehaviorMetrics(tasks, suivis);
            JSONObject aiInsights = callAiApi(metrics);

            return saveProfile(userId, metrics, aiInsights);

        } catch (Exception e) {
            System.out.println("Error computing profile: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> computeBehaviorMetrics(List<tache> tasks, List<suiviTache> suivis) {

        Map<String, Object> metrics = new HashMap<>();

        int totalTasks = tasks.size();

        if (totalTasks == 0) {
            metrics.put("completionRate", 0);
            metrics.put("abandonmentRate", 0);
            metrics.put("averageStartDelayMinutes", 0);
            metrics.put("pauseFrequency", 0);
            metrics.put("mostProductiveHour", null);
            metrics.put("mostProductiveDayOfWeek", null);
            metrics.put("abandonmentRateByType", new HashMap<>());
            metrics.put("completionRateByType", new HashMap<>());
            metrics.put("averageStartDelayByType", new HashMap<>());
            return metrics;
        }

        // ========================
        // Abandonment rate
        // ========================
        long abandonedTasks = tasks.stream()
                .filter(t -> "ABANDON".equals(t.getStatut())
                        || (t.getDate_fin() != null
                        && t.getDate_fin().before(new java.util.Date())
                        && !"TERMINE".equals(t.getStatut())))
                .count();

        double abandonmentRate = (double) abandonedTasks / totalTasks;

        // ========================
        // Completion rate
        // ========================
        long completedTasks = tasks.stream()
                .filter(t -> "TERMINE".equals(t.getStatut()))
                .count();

        double completionRate = (double) completedTasks / totalTasks;

        // ========================
        // Average start delay
        // ========================
        List<Double> startDelays = new ArrayList<>();

        for (tache task : tasks) {

            List<suiviTache> taskSuivis = suivis.stream()
                    .filter(s -> s.getTache().getId() == task.getId()
                            && "EN_COURS".equals(s.getNouveauStatut()))
                    .toList();

            if (!taskSuivis.isEmpty() && task.getDate_debut() != null) {

                long firstStart = taskSuivis.stream()
                        .mapToLong(s -> s.getDateAction().getTime())
                        .min()
                        .orElse(0);

                long delay = (firstStart - task.getDate_debut().getTime()) / 60000;

                startDelays.add(Math.max(0.0, (double) delay));
            }
        }

        double averageStartDelay = startDelays.isEmpty()
                ? 0
                : startDelays.stream().mapToDouble(Double::doubleValue).sum() / startDelays.size();

        // ========================
        // Pause frequency
        // ========================
        long pauseCount = suivis.stream()
                .filter(s -> "PAUSED".equals(s.getNouveauStatut()))
                .count();

        double pauseFrequency = (double) pauseCount / totalTasks;

        // ========================
        // Most productive hour/day
        // ========================
        Map<Integer, Integer> hourCounts = new HashMap<>();
        Map<Integer, Integer> dayCounts = new HashMap<>();

        for (suiviTache s : suivis) {
            if ("EN_COURS".equals(s.getNouveauStatut()) || "TERMINE".equals(s.getNouveauStatut())) {

                LocalDateTime dt = s.getDateAction()
                        .toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
                int hour = dt.getHour();
                int day = dt.getDayOfWeek().getValue();

                hourCounts.put(hour, hourCounts.getOrDefault(hour, 0) + 1);
                dayCounts.put(day, dayCounts.getOrDefault(day, 0) + 1);
            }
        }

        Integer mostProductiveHour = hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Integer mostProductiveDay = dayCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // ========================
        // Task type metrics
        // ========================
        String[] types = {"MANUEL", "REUNION", "REVISION", "SANTE", "EMPLOI"};

        Map<String, Double> abandonmentByType = new HashMap<>();
        Map<String, Double> completionByType = new HashMap<>();
        Map<String, Double> delayByType = new HashMap<>();

        for (String type : types) {

            List<tache> tasksOfType = tasks.stream()
                    .filter(t -> type.equals(t.getType()))
                    .toList();

            int count = tasksOfType.size();

            if (count == 0) {
                abandonmentByType.put(type, 0.0);
                completionByType.put(type, 0.0);
                delayByType.put(type, 0.0);
                continue;
            }

            long abandoned = tasksOfType.stream()
                    .filter(t -> "ABANDON".equals(t.getStatut()))
                    .count();

            long completed = tasksOfType.stream()
                    .filter(t -> "TERMINE".equals(t.getStatut()))
                    .count();

            abandonmentByType.put(type, (double) abandoned / count);
            completionByType.put(type, (double) completed / count);

            List<Double> typeDelays = new ArrayList<>();

            for (tache task : tasksOfType) {

                List<suiviTache> taskSuivis = suivis.stream()
                        .filter(s -> s.getTache().getId() == task.getId()
                                && "EN_COURS".equals(s.getNouveauStatut()))
                        .toList();

                if (!taskSuivis.isEmpty() && task.getDate_debut() != null) {

                    long firstStart = taskSuivis.stream()
                            .mapToLong(s -> s.getDateAction().getTime())
                            .min()
                            .orElse(0);

                    long delay = (firstStart - task.getDate_debut().getTime()) / 60000;
                    typeDelays.add(Math.max(0.0, (double) delay));
                }
            }

            double avgDelay = typeDelays.isEmpty()
                    ? 0
                    : typeDelays.stream().mapToDouble(Double::doubleValue).sum() / typeDelays.size();

            delayByType.put(type, avgDelay);
        }

        // ========================
        // RETURN (same as PHP)
        // ========================
        metrics.put("completionRate", completionRate);
        metrics.put("abandonmentRate", abandonmentRate);
        metrics.put("averageStartDelayMinutes", averageStartDelay);
        metrics.put("pauseFrequency", pauseFrequency);
        metrics.put("mostProductiveHour", mostProductiveHour);
        metrics.put("mostProductiveDayOfWeek", mostProductiveDay);
        metrics.put("abandonmentRateByType", abandonmentByType);
        metrics.put("completionRateByType", completionByType);
        metrics.put("averageStartDelayByType", delayByType);

        return metrics;
    }

    private JSONObject callAiApi(Map<String, Object> metrics) {
        JSONObject result = new JSONObject();

        if (groqKey == null || groqKey.isEmpty()) {
            try {
                result.put("weeklyProductivitySummary",
                        "Analyse IA indisponible : clé API non configurée.");
                result.put("behavioralAdvice",
                        "Veuillez configurer une clé API pour utiliser cette fonctionnalité.");
            } catch (JSONException e) {
                System.out.println("JSON Error: " + e.getMessage());
            }
            return result;
        }
        try {

            String prompt = """
        Tu es un analyste de productivité académique.

        Sur la base des métriques suivantes, fournis :
        1) Un résumé concis (max 120 mots)
        2) Des conseils actionnables (max 120 mots)

        IMPORTANT:
        - Réponds STRICTEMENT en JSON valide
        - N'utilise PAS de tableau pour les conseils
        - N'ajoute AUCUN texte en dehors du JSON
        - Format EXACT attendu :
        
        {
          "weeklyProductivitySummary": "texte",
          "behavioralAdvice": "texte"
        }

        Métriques :
        - Taux de complétion : %s
        - Taux d'abandon : %s
        - Délai moyen de démarrage : %s
        - Fréquence de pause : %s
        - Heure la plus productive : %s
        - Jour le plus productif : %s

        Abandon par type : %s
        Complétion par type : %s
        Délai par type : %s
        """.formatted(
                    metrics.get("completionRate"),
                    metrics.get("abandonmentRate"),
                    metrics.get("averageStartDelayMinutes"),
                    metrics.get("pauseFrequency"),
                    metrics.get("mostProductiveHour"),
                    metrics.get("mostProductiveDayOfWeek"),
                    metrics.get("abandonmentRateByType"),
                    metrics.get("completionRateByType"),
                    metrics.get("averageStartDelayByType")
            );

            JSONObject body = new JSONObject();
            body.put("model", "llama-3.1-8b-instant");

            body.put("messages", new org.json.JSONArray()
                    .put(new JSONObject()
                            .put("role", "system")
                            .put("content", "Tu analyses les performances des étudiants et réponds uniquement en JSON valide."))
                    .put(new JSONObject()
                            .put("role", "user")
                            .put("content", prompt))
            );

            body.put("temperature", 0.7);
            body.put("max_tokens", 500);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + groqKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                result.put("weeklyProductivitySummary",
                        "Analyse IA temporairement indisponible.");
                result.put("behavioralAdvice",
                        "Veuillez réessayer plus tard.");
                return result;
            }

            JSONObject data = new JSONObject(response.body());

            String content = data.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // ---- Clean response ----
            content = content.trim();

            // Extract JSON part if model adds text
            if (content.contains("{")) {
                content = content.substring(content.indexOf("{"),
                        content.lastIndexOf("}") + 1);
            }

            JSONObject parsed = new JSONObject(content);

            // ✅ HANDLE SUMMARY
            String summary;

            if (parsed.has("weeklyProductivitySummary")) {
                summary = parsed.getString("weeklyProductivitySummary");
            } else {
                // fallback → convert whole JSON to readable text
                summary = parsed.toString(2);
            }

            // ✅ HANDLE ADVICE
            String advice;

            if (parsed.has("behavioralAdvice")) {

                Object adviceObj = parsed.get("behavioralAdvice");

                if (adviceObj instanceof org.json.JSONArray arr) {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < arr.length(); i++) {
                        sb.append("- ").append(arr.getString(i)).append("\n");
                    }

                    advice = sb.toString();

                } else {
                    advice = adviceObj.toString();
                }

            } else {
                advice = "Conseils non disponibles.";
            }

            result.put("weeklyProductivitySummary", summary);
            result.put("behavioralAdvice", advice);
            return result;

        } catch (Exception e) {
            System.out.println("AI ERROR: " + e.getMessage());

            try {
                result.put("weeklyProductivitySummary",
                        "Analyse IA temporairement indisponible.");
                result.put("behavioralAdvice",
                        "Veuillez réessayer plus tard.");
            } catch (JSONException ex) {
                System.out.println("JSON Error: " + ex.getMessage());
            }

            return result;
        }
    }

    private StudentIntelligenceProfile saveProfile(int userId, Map<String, Object> metrics, JSONObject aiInsights){

        StudentIntelligenceProfile sp = new StudentIntelligenceProfile();

        // ⚠️ assuming you have a User reference method somewhere
        // You MUST fetch user entity (adjust to your project)
        // User user = userService.findById(userId);
        sp.setUserId(userId);
        sp.setCompletionRate((Double) metrics.get("completionRate"));
        sp.setAbandonmentRate((Double) metrics.get("abandonmentRate"));
        sp.setAverageStartDelayMinutes((Double) metrics.get("averageStartDelayMinutes"));
        sp.setPauseFrequency((Double) metrics.get("pauseFrequency"));

        sp.setMostProductiveHour((Integer) metrics.get("mostProductiveHour"));
        sp.setMostProductiveDayOfWeek((Integer) metrics.get("mostProductiveDayOfWeek"));
        // Maps (no casting issues because Java generics are erased)
        sp.setAbandonmentRateByType((Map<String, Double>) metrics.get("abandonmentRateByType"));

        sp.setCompletionRateByType((Map<String, Double>) metrics.get("completionRateByType"));

        sp.setAverageStartDelayByType((Map<String, Double>) metrics.get("averageStartDelayByType"));

        // AI INSIGHTS
        sp.setWeeklyProductivitySummary(aiInsights.optString("weeklyProductivitySummary", null));

        sp.setBehavioralAdvice(aiInsights.optString("behavioralAdvice", null));

        // analyzedAt
        sp.setAnalyzedAt(Timestamp.valueOf(LocalDateTime.now()));

        // ⚠️ persist (depends on your architecture)
        try {
            si.add(sp); // or em.persist(sp) depending on your service
        } catch (Exception e) {
            System.out.println("SAVE ERROR: " + e.getMessage());
        }

        return sp;
    }



}
