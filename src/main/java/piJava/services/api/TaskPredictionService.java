package piJava.services.api;

import piJava.entities.tache;
import piJava.entities.StudentIntelligenceProfile;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import piJava.services.StudentIntelligenceProfileService;
import piJava.services.TacheService;

public class TaskPredictionService {

    private static final String FLASK_API_URL = "http://localhost:5000/predict";
    private final ObjectMapper mapper = new ObjectMapper();

    private final StudentIntelligenceProfileService profileService;
    private final TacheService tacheService;
    DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    public TaskPredictionService() {
        this.profileService = new StudentIntelligenceProfileService();
        this.tacheService = new TacheService();
    }

    /**
     * Main method to predict task completion probability
     */
    public TaskPredictionResponse predictTaskCompletion(tache task) {
        try {
            // Build JSON payload
            ObjectNode payload = buildPredictionPayload(task);

            // Call Flask API
            String jsonResponse = callFlaskAPI(payload.toString());

            // Parse response
            return mapper.readValue(jsonResponse, TaskPredictionResponse.class);

        } catch (Exception e) {
            System.err.println("❌ Prediction API Error: " + e.getMessage());
            e.printStackTrace();

            // Return default prediction on error
            return new TaskPredictionResponse(0, 0.5, 0.5, "MEDIUM");
        }
    }

    /**
     * Build the complete feature payload for the ML model
     */
    private ObjectNode buildPredictionPayload(tache task) throws Exception {
        ObjectNode payload = mapper.createObjectNode();

        // ========================================
        // 1. BASIC TASK INFO
        // ========================================
        payload.put("date_debut",
                task.getDate_debut().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(fmt)
        );

        payload.put("date_fin",
                task.getDate_fin().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                        .format(fmt)
        );
        payload.put("type", task.getType());
        payload.put("priorite", task.getPriorite());

        // ========================================
        // 2. USER PROFILE FEATURES
        // ========================================
        StudentIntelligenceProfile profile = profileService.showLatestPerUser(task.getUser_id());

        if (profile != null) {
            payload.put("completion_rate", profile.getCompletionRate());
            payload.put("abandonment_rate", profile.getAbandonmentRate());
            payload.put("average_start_delay_minutes", profile.getAverageStartDelayMinutes());
            payload.put("average_completion_duration_minutes", profile.getAverageCompletionDurationMinutes());
            payload.put("pause_frequency", profile.getPauseFrequency());
            payload.put("most_productive_hour", profile.getMostProductiveHour());
            payload.put("most_productive_day_of_week", profile.getMostProductiveDayOfWeek());
        } else {
            // Default values if no profile exists
            payload.put("completion_rate", 0.5);
            payload.put("abandonment_rate", 0.25);
            payload.put("average_start_delay_minutes", 60.0);
            payload.put("average_completion_duration_minutes", 120.0);
            payload.put("pause_frequency", 2.0);
            payload.put("most_productive_hour", 14);
            payload.put("most_productive_day_of_week", 2);
        }

        // ========================================
        // 3. HISTORICAL FEATURES
        // ========================================
        HistoricalFeatures historical = calculateHistoricalFeatures(
                task.getUser_id(),
                task.getDate_debut()  // Now accepts Date
        );
        payload.put("last_5_tasks_abandonment_rate", historical.last5TasksAbandonmentRate);
        payload.put("last_7_days_completion_rate", historical.last7DaysCompletionRate);

        // ========================================
        // 4. PAUSE FREQUENCY THRESHOLD (for high_pauses calculation)
        // ========================================
        payload.put("pause_frequency_threshold", calculatePauseQ75());

        return payload;
    }


    /**
     * Calculate historical features from user's past tasks
     */
    private HistoricalFeatures calculateHistoricalFeatures(int userId, Date currentDate) {
        HistoricalFeatures features = new HistoricalFeatures();

        try {
            // Convert Date to Timestamp for database comparison
            Timestamp currentTimestamp = currentDate instanceof Timestamp
                    ? (Timestamp) currentDate
                    : new Timestamp(currentDate.getTime());

            LocalDateTime now = currentTimestamp.toLocalDateTime();

            // ========================================
            // LAST 5 TASKS ABANDONMENT RATE
            // ========================================
            List<tache> allUserTasks = tacheService.showUserTasks(userId);

            // Filter tasks before current date and sort by date_debut DESC
            List<tache> pastTasks = allUserTasks.stream()
                    .filter(t -> t.getDate_debut().before(currentTimestamp))
                    .sorted((t1, t2) -> t2.getDate_debut().compareTo(t1.getDate_debut()))
                    .limit(5)
                    .toList();

            if (!pastTasks.isEmpty()) {
                long abandonCount = pastTasks.stream()
                        .filter(t -> "ABANDON".equals(t.getStatut()))
                        .count();
                features.last5TasksAbandonmentRate = (double) abandonCount / pastTasks.size();
            } else {
                features.last5TasksAbandonmentRate = 0.0;
            }

            // ========================================
            // LAST 7 DAYS COMPLETION RATE
            // ========================================
            LocalDateTime sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
            List<tache> last7DaysTasks = tacheService.getTasksByUserSince(userId, sevenDaysAgo);

            // Filter tasks that are completed or abandoned (exclude in-progress)
            List<tache> finishedTasks = last7DaysTasks.stream()
                    .filter(t -> "TERMINE".equals(t.getStatut()) || "ABANDON".equals(t.getStatut()))
                    .filter(t -> t.getDate_debut().before(currentTimestamp)) // Only past tasks
                    .toList();

            if (!finishedTasks.isEmpty()) {
                long completeCount = finishedTasks.stream()
                        .filter(t -> "TERMINE".equals(t.getStatut()))
                        .count();
                features.last7DaysCompletionRate = (double) completeCount / finishedTasks.size();
            } else {
                // Fallback to profile completion rate
                StudentIntelligenceProfile profile = profileService.showLatestPerUser(userId);
                features.last7DaysCompletionRate = profile != null ? profile.getCompletionRate() : 0.5;
            }

        } catch (Exception e) {
            System.err.println("Error calculating historical features: " + e.getMessage());
            features.last5TasksAbandonmentRate = 0.0;
            features.last7DaysCompletionRate = 0.5;
        }

        return features;
    }


    /**
     * Calculate 75th percentile of pause frequency across all users
     * (This should ideally be cached or pre-calculated)
     */
    private double calculatePauseQ75() {
        try {
            // For simplicity, using a fixed threshold
            // In production, calculate from all profiles
            return 4.0;
        } catch (Exception e) {
            return 4.0; // default threshold
        }
    }

    /**
     * HTTP call to Flask API
     */
    private String callFlaskAPI(String jsonPayload) throws Exception {
        URL url = new URL(FLASK_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send payload
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                return scanner.useDelimiter("\\A").next();
            }
        } else {
            throw new Exception("Flask API returned error code: " + responseCode);
        }
    }

    /**
     * Inner class to hold historical features
     */
    private static class HistoricalFeatures {
        double last5TasksAbandonmentRate = 0.0;
        double last7DaysCompletionRate = 0.5;
    }

    /**
     * Response DTO matching Flask API response
     */
    public static class TaskPredictionResponse {
        private int prediction;
        private double probability_complete;
        private double probability_abandon;
        private String risk_level;

        // Default constructor for Jackson
        public TaskPredictionResponse() {}

        public TaskPredictionResponse(int prediction, double probability_complete,
                                      double probability_abandon, String risk_level) {
            this.prediction = prediction;
            this.probability_complete = probability_complete;
            this.probability_abandon = probability_abandon;
            this.risk_level = risk_level;
        }

        // Getters and setters
        public int getPrediction() { return prediction; }
        public void setPrediction(int prediction) { this.prediction = prediction; }

        public double getProbability_complete() { return probability_complete; }
        public void setProbability_complete(double probability_complete) {
            this.probability_complete = probability_complete;
        }

        public double getProbability_abandon() { return probability_abandon; }
        public void setProbability_abandon(double probability_abandon) {
            this.probability_abandon = probability_abandon;
        }

        public String getRisk_level() { return risk_level; }
        public void setRisk_level(String risk_level) { this.risk_level = risk_level; }

        @Override
        public String toString() {
            return String.format("Prediction{prediction=%d, complete=%.2f%%, abandon=%.2f%%, risk=%s}",
                    prediction, probability_complete * 100, probability_abandon * 100, risk_level);
        }
    }
}