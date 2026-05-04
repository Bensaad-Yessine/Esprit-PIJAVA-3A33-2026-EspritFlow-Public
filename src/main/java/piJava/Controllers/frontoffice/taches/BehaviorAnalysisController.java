package piJava.Controllers.frontoffice.taches;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import piJava.entities.StudentIntelligenceProfile;
import piJava.services.api.BehaviorAnalysisService;

import java.util.Map;

public class BehaviorAnalysisController {

    // Header
    @FXML private Label loadingLabel;

    // Statistics Cards
    @FXML private Label completionRateValue;
    @FXML private Label completionRateTrend;
    @FXML private Label completionRateDesc;
    @FXML private ProgressBar completionRateBar;

    @FXML private Label abandonmentRateValue;
    @FXML private Label abandonmentRateTrend;
    @FXML private Label abandonmentRateDesc;
    @FXML private ProgressBar abandonmentRateBar;

    @FXML private Label startDelayValue;
    @FXML private Label startDelayTrend;
    @FXML private Label startDelayDesc;
    @FXML private ProgressBar startDelayBar;

    @FXML private Label pauseFrequencyValue;
    @FXML private Label pauseFrequencyTrend;
    @FXML private Label pauseFrequencyDesc;
    @FXML private ProgressBar pauseFrequencyBar;

    // Insights
    @FXML private Label productiveHourValue;
    @FXML private Label productiveDayValue;

    // Task Type Stats Container
    @FXML private VBox taskTypeStats;

    // AI Summary & Advice
    @FXML private TextArea summaryArea;
    @FXML private TextArea adviceArea;

    private final BehaviorAnalysisService service = new BehaviorAnalysisService();
    private int userId;

    public void loadDataForUser(int userId) {
        this.userId = userId;

        // Show loading state
        setLoadingState(true);

        // Run analysis in background thread
        new Thread(() -> {
            try {
                StudentIntelligenceProfile profile = service.getOrComputeProfile(userId);

                Platform.runLater(() -> {
                    setLoadingState(false);

                    if (profile != null) {
                        populateStatistics(profile);
                        populateInsights(profile);
                        populateTaskTypeStats(profile);
                        populateAISummary(profile);
                    } else {
                        showError("Impossible de charger les données d'analyse");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    showError("Erreur lors de l'analyse: " + e.getMessage());
                });
            }
        }).start();
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            loadingLabel.setText("⏳ Analyse en cours...");
            loadingLabel.setVisible(true);
        } else {
            loadingLabel.setVisible(false);
        }
    }

    private void populateStatistics(StudentIntelligenceProfile profile) {
        // Completion Rate
        double completionRate = profile.getCompletionRate();
        completionRateValue.setText(formatPercentage(completionRate));
        completionRateBar.setProgress(completionRate);
        completionRateDesc.setText(
                String.format("%.0f%% des tâches terminées avec succès", completionRate * 100)
        );

        // Set trend indicator
        if (completionRate >= 0.7) {
            completionRateTrend.setText("↑");
            completionRateTrend.getStyleClass().add("positive");
        } else if (completionRate >= 0.4) {
            completionRateTrend.setText("→");
            completionRateTrend.getStyleClass().add("neutral");
        } else {
            completionRateTrend.setText("↓");
            completionRateTrend.getStyleClass().add("negative");
        }

        // Abandonment Rate
        double abandonmentRate = profile.getAbandonmentRate();
        abandonmentRateValue.setText(formatPercentage(abandonmentRate));
        abandonmentRateBar.setProgress(abandonmentRate);
        abandonmentRateDesc.setText(
                String.format("%.0f%% des tâches abandonnées", abandonmentRate * 100)
        );

        if (abandonmentRate <= 0.2) {
            abandonmentRateTrend.setText("↓");
            abandonmentRateTrend.getStyleClass().add("positive");
        } else if (abandonmentRate <= 0.4) {
            abandonmentRateTrend.setText("→");
            abandonmentRateTrend.getStyleClass().add("neutral");
        } else {
            abandonmentRateTrend.setText("↑");
            abandonmentRateTrend.getStyleClass().add("negative");
        }

        // Start Delay
        double startDelay = profile.getAverageStartDelayMinutes();
        startDelayValue.setText(formatMinutes(startDelay));

        // Normalize delay for progress bar (assuming max 120 minutes)
        double normalizedDelay = Math.min(startDelay / 120.0, 1.0);
        startDelayBar.setProgress(normalizedDelay);

        startDelayDesc.setText(
                String.format("Temps moyen de %.0f minutes avant de commencer", startDelay)
        );

        if (startDelay <= 30) {
            startDelayTrend.setText("↓");
            startDelayTrend.getStyleClass().add("positive");
        } else if (startDelay <= 60) {
            startDelayTrend.setText("→");
            startDelayTrend.getStyleClass().add("neutral");
        } else {
            startDelayTrend.setText("↑");
            startDelayTrend.getStyleClass().add("negative");
        }

        // Pause Frequency
        double pauseFreq = profile.getPauseFrequency();
        pauseFrequencyValue.setText(String.format("%.1f", pauseFreq));
        pauseFrequencyBar.setProgress(Math.min(pauseFreq / 3.0, 1.0));
        pauseFrequencyDesc.setText("Pauses par tâche en moyenne");

        if (pauseFreq <= 1.0) {
            pauseFrequencyTrend.setText("↓");
            pauseFrequencyTrend.getStyleClass().add("positive");
        } else if (pauseFreq <= 2.0) {
            pauseFrequencyTrend.setText("→");
            pauseFrequencyTrend.getStyleClass().add("neutral");
        } else {
            pauseFrequencyTrend.setText("↑");
            pauseFrequencyTrend.getStyleClass().add("negative");
        }
    }

    private void populateInsights(StudentIntelligenceProfile profile) {
        // Most Productive Hour
        Integer hour = profile.getMostProductiveHour();
        if (hour != null) {
            productiveHourValue.setText(String.format("%02d:00 - %02d:00", hour, hour + 1));
        } else {
            productiveHourValue.setText("Données insuffisantes");
        }

        // Most Productive Day
        Integer day = profile.getMostProductiveDayOfWeek();
        if (day != null) {
            String[] days = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
            productiveDayValue.setText(days[day % 7]);
        } else {
            productiveDayValue.setText("Données insuffisantes");
        }
    }

    private void populateTaskTypeStats(StudentIntelligenceProfile profile) {
        taskTypeStats.getChildren().clear();

        Map<String, Double> completionByType = profile.getCompletionRateByType();
        Map<String, Double> abandonmentByType = profile.getAbandonmentRateByType();

        if (completionByType == null || completionByType.isEmpty()) {
            Label noData = new Label("Aucune donnée de type de tâche disponible");
            noData.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px;");
            taskTypeStats.getChildren().add(noData);
            return;
        }

        String[] typeNames = {
                "MANUEL", "REUNION", "REVISION", "SANTE", "EMPLOI"
        };

        String[] typeEmojis = {
                "📝", "👥", "📚", "💪", "💼"
        };

        String[] typeFrench = {
                "Manuel", "Réunion", "Révision", "Santé", "Emploi"
        };

        for (int i = 0; i < typeNames.length; i++) {
            String type = typeNames[i];

            Double completion = completionByType.getOrDefault(type, 0.0);
            Double abandonment = abandonmentByType.getOrDefault(type, 0.0);

            HBox row = createTaskTypeRow(
                    typeEmojis[i] + " " + typeFrench[i],
                    completion,
                    abandonment
            );

            taskTypeStats.getChildren().add(row);
        }
    }

    private HBox createTaskTypeRow(String typeName, double completion, double abandonment) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("task-type-row");
        row.setPadding(new Insets(8, 0, 8, 0));

        // Type Label
        Label typeLabel = new Label(typeName);
        typeLabel.getStyleClass().add("task-type-label");
        typeLabel.setMinWidth(150);

        // Completion Badge
        Label completionLabel = new Label("✅ " + formatPercentage(completion));
        completionLabel.getStyleClass().add("task-type-badge");
        completionLabel.setStyle(
                completionLabel.getStyle() +
                        "-fx-background-color: " + getCompletionColor(completion) + ";"
        );

        // Abandonment Badge
        Label abandonmentLabel = new Label("⚠️ " + formatPercentage(abandonment));
        abandonmentLabel.getStyleClass().add("task-type-badge");
        abandonmentLabel.setStyle(
                abandonmentLabel.getStyle() +
                        "-fx-background-color: " + getAbandonmentColor(abandonment) + ";"
        );

        row.getChildren().addAll(typeLabel, completionLabel, abandonmentLabel);

        return row;
    }

    private void populateAISummary(StudentIntelligenceProfile profile) {
        String summary = profile.getWeeklyProductivitySummary();
        String advice = profile.getBehavioralAdvice();

        // Clean and format summary
        if (summary != null && !summary.isEmpty()) {
            summary = cleanText(summary);
            summaryArea.setText(summary);
        } else {
            summaryArea.setText("Aucun résumé disponible pour le moment.\n\nContinuez à utiliser l'application pour générer des analyses personnalisées.");
        }

        // Clean and format advice
        if (advice != null && !advice.isEmpty()) {
            advice = cleanText(advice);
            adviceArea.setText(advice);
        } else {
            adviceArea.setText("Aucun conseil disponible pour le moment.\n\nPlus vous utilisez l'application, plus les conseils seront pertinents.");
        }
    }

    private String cleanText(String text) {
        if (text == null) return "";

        return text
                .replace("{", "")
                .replace("}", "")
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .trim();
    }

    private String formatPercentage(double value) {
        return String.format("%.0f%%", value * 100);
    }

    private String formatMinutes(double minutes) {
        if (minutes < 60) {
            return String.format("%.0f min", minutes);
        } else {
            double hours = minutes / 60.0;
            return String.format("%.1f h", hours);
        }
    }

    private String getCompletionColor(double rate) {
        if (rate >= 0.7) return "#c6f6d5"; // Green
        if (rate >= 0.4) return "#fef5e7"; // Yellow
        return "#fed7d7"; // Red
    }

    private String getAbandonmentColor(double rate) {
        if (rate <= 0.2) return "#c6f6d5"; // Green
        if (rate <= 0.4) return "#fef5e7"; // Yellow
        return "#fed7d7"; // Red
    }

    private void showError(String message) {
        loadingLabel.setText("❌ " + message);
        loadingLabel.setStyle("-fx-text-fill: #e53e3e;");
        loadingLabel.setVisible(true);
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) summaryArea.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleRefresh() {
        loadDataForUser(userId);
    }

    @FXML
    public void handleExport() {
        // TODO: Implement PDF export functionality
        // You can use libraries like iText or Apache PDFBox
        loadingLabel.setText("📄 Fonction d'export en développement");
        loadingLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> loadingLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}