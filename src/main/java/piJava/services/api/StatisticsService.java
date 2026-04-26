package piJava.services.api;

import piJava.entities.tache;
import piJava.services.TacheService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        LocalDateTime since = LocalDateTime.now().minusDays(days - 1);

        List<tache> tasks = tacheService.getTasksByUserSince(userId, since);

        Map<String, Map<String, Integer>> result = new TreeMap<>();

        for (tache t : tasks) {

            String date = ((java.sql.Timestamp) t.getDate_debut())
                    .toLocalDateTime()
                    .toLocalDate()
                    .toString();

            result.putIfAbsent(date, new HashMap<>());
            Map<String, Integer> day = result.get(date);

            day.put("created", day.getOrDefault("created", 0) + 1);

            if ("TERMINE".equals(t.getStatut())) {
                day.put("completed", day.getOrDefault("completed", 0) + 1);
            }

            if ("ABANDON".equals(t.getStatut())) {
                day.put("abandoned", day.getOrDefault("abandoned", 0) + 1);
            }
        }

        return result;
    }

}