package piJava.services.api;

import piJava.entities.tache;
import piJava.services.TacheService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class StatisticsService {

    private TacheService tacheService;

    public StatisticsService() {
        this.tacheService = new TacheService();
    }

    public Map<String, Object> getUserStats(int userId) throws SQLException {

        List<tache> tasks = tacheService.showUserTasks(userId);

        int total = tasks.size();

        int completed = 0;
        int abandoned = 0;
        int inProgress = 0;
        int overdue = 0;
        int paused = 0;
        int highPriority = 0;

        for (tache t : tasks) {

            // ----- Status -----
            switch (t.getStatut()) {
                case "TERMINE":
                    completed++;
                    break;
                case "ABANDON":
                    abandoned++;
                    break;
                case "EN_COURS":
                    inProgress++;
                    break;
                case "EN_RETARD":
                    overdue++;
                    break;
                case "PAUSED":
                    paused++;
                    break;
            }

            // ----- Priority -----
            if ("ELEVEE".equals(t.getPriorite())) {
                highPriority++;
            }
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", total);
        counts.put("completed", completed);
        counts.put("abandoned", abandoned);
        counts.put("inProgress", inProgress);
        counts.put("overdue", overdue);
        counts.put("paused", paused);
        counts.put("highPriority", highPriority);

        Map<String, Object> result = new HashMap<>();
        result.put("counts", counts);

        return result;
    }

    public Map<String, Map<String, Integer>> getProgressLastDays(int userId, int days) {

        // Use LinkedHashMap to maintain insertion order (chronological)
        Map<String, Map<String, Integer>> stats = new LinkedHashMap<>();

        // Initialize all days with zero counts
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString(); // Format: YYYY-MM-DD
            Map<String, Integer> dayStats = new HashMap<>();
            dayStats.put("created", 0);
            dayStats.put("completed", 0);
            dayStats.put("abandoned", 0);
            stats.put(dateStr, dayStats);
        }

        // Get ALL tasks for this user (not filtered by date)
        List<tache> allTasks;
        try {
            allTasks = tacheService.showUserTasks(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return stats; // Return empty stats on error
        }

        // Process each task
        for (tache task : allTasks) {
            try {
                // 1. Created Stats
                Date createdDateRaw = task.getCreated_at() != null ? task.getCreated_at() : task.getDate_debut();
                if (createdDateRaw != null) {
                    LocalDate createdDate = convertToLocalDate(createdDateRaw);
                    String createdDateStr = createdDate.toString();
                    if (stats.containsKey(createdDateStr)) {
                        Map<String, Integer> dayStats = stats.get(createdDateStr);
                        dayStats.put("created", dayStats.get("created") + 1);
                    }
                }

                // 2. Completed Stats
                if ("TERMINE".equals(task.getStatut())) {
                    Date completedDateRaw = task.getUpdated_at() != null ? task.getUpdated_at() : task.getDate_fin();
                    if (completedDateRaw != null) {
                        LocalDate completedDate = convertToLocalDate(completedDateRaw);
                        String completedDateStr = completedDate.toString();
                        if (stats.containsKey(completedDateStr)) {
                            Map<String, Integer> dayStats = stats.get(completedDateStr);
                            dayStats.put("completed", dayStats.get("completed") + 1);
                        }
                    }
                }

                // 3. Abandoned Stats
                if ("ABANDON".equals(task.getStatut())) {
                    Date abandonedDateRaw = task.getUpdated_at() != null ? task.getUpdated_at() : task.getDate_fin();
                    if (abandonedDateRaw != null) {
                        LocalDate abandonedDate = convertToLocalDate(abandonedDateRaw);
                        String abandonedDateStr = abandonedDate.toString();
                        if (stats.containsKey(abandonedDateStr)) {
                            Map<String, Integer> dayStats = stats.get(abandonedDateStr);
                            dayStats.put("abandoned", dayStats.get("abandoned") + 1);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing stats for task " + task.getId() + ": " + e.getMessage());
            }
        }

        return stats;
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

}