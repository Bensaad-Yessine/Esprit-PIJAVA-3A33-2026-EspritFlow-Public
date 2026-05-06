package piJava.Controllers.frontoffice.taches;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.chart.*;

import javafx.scene.shape.Circle;
import javafx.util.Duration;
import piJava.entities.Notification;
import piJava.services.NotificationService;
import piJava.services.api.NotifsService;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import piJava.Controllers.frontoffice.taches.TacheEditController;
import piJava.Controllers.frontoffice.taches.TachesDetailsController;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.user;
import piJava.services.TacheService;
import piJava.entities.tache;
import piJava.services.api.BehaviorAnalysisService;
import piJava.services.api.StatisticsService;
import piJava.services.api.WeatherAiService;
import piJava.utils.SessionManager;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TachesController implements Initializable {

    private final user currentUser = SessionManager.getInstance().getCurrentUser();
    private final int currentUserId = currentUser.getId();

    @FXML private ListView<tache> activeTasksList;
    @FXML private ListView<tache> archivedTasksList;

    @FXML private Label lblActiveCount;
    @FXML private Label lblArchivedCount;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbPriority;
    @FXML private ComboBox<String> cmbSort;
    @FXML private Button btnResetFilters;
    @FXML private Button btnWeather;
    @FXML private Button btnWeeklyAnalysis;

    @FXML private BarChart<String, Number> statusChart;
    @FXML private PieChart priorityChart;
    @FXML private LineChart<String, Number> progressChart;

    @FXML private Label lblTotalTasks;
    @FXML private Label lblInProgress;
    @FXML private Label lblCompleted;
    @FXML private Label lblUrgent;

    @FXML private VBox notificationArea;
    @FXML private VBox mainContent;
    @FXML private StackPane notificationBtn;
    @FXML private Circle notificationDot;
    private boolean notificationsVisible = false;
    @FXML private ListView<Notification> notificationsList;


    private List<tache> allTasks = new ArrayList<>();
    private List<tache> activeTasks = new ArrayList<>();
    private List<tache> archivedTasks = new ArrayList<>();

    private FrontSidebarController sidebarController;
    private StatisticsService statsService = new StatisticsService();
    private TacheService ts = new TacheService();
    private NotifsService ns = new NotifsService();
    private NotificationService nss = new NotificationService();
    // Static variables to temporarily store task data
    private static tache currentTaskForDetails;
    private static tache currentTaskForEdit;


    private final ScheduledExecutorService notifScheduler =
            Executors.newSingleThreadScheduledExecutor();

    public void setSidebarController(FrontSidebarController sidebarcontroller) {
        this.sidebarController = sidebarcontroller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("INIT START");
        System.out.println("notificationsList = " + notificationsList);
        System.out.println("notificationArea = " + notificationArea);

        try {
            // 1. Notifications setup FIRST
            setupNotificationsList();
            startNotificationAutoRefresh();

            loadTasks();

            notificationArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {

                    newScene.setOnMouseClicked(event -> {

                        if (notificationsVisible &&
                                !notificationArea.localToScene(notificationArea.getBoundsInLocal())
                                        .contains(event.getSceneX(), event.getSceneY())) {

                            notificationsVisible = false;
                            notificationArea.setVisible(false);
                            notificationArea.setManaged(false);
                        }
                    });
                }
            });
            cmbPriority.setValue("Toutes");
            cmbSort.setValue("Date de création");

            txtSearch.textProperty().addListener((obs, o, n) -> refreshTasks());
            cmbPriority.setOnAction(e -> refreshTasks());
            cmbSort.setOnAction(e -> refreshTasks());

            btnResetFilters.setOnAction(e -> {
                txtSearch.clear();
                cmbPriority.setValue("Toutes");
                cmbSort.setValue("Date de création");
                refreshTasks();
            });

            refreshTasks();
            loadStatsAndCharts();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadTasks() throws SQLException {
        allTasks = ts.showUserTasks(currentUserId);
        activeTasks.clear();
        archivedTasks.clear();

        List<String> activeStatus = Arrays.asList("EN_COURS", "A_FAIRE", "PAUSED");

        for (tache t : allTasks) {
            if (activeStatus.contains(t.getStatut())) {
                activeTasks.add(t);
            } else {
                archivedTasks.add(t);
            }
        }
    }

    private void refreshTasks() {

        String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase();
        String priority = cmbPriority.getValue();

        List<tache> filteredActive = new ArrayList<>(activeTasks.stream()
                .filter(t -> filterTask(t, search, priority))
                .toList());

        List<tache> filteredArchived = new ArrayList<>(archivedTasks.stream()
                .filter(t -> filterTask(t, search, priority))
                .toList());

        sortTasks(filteredActive);
        sortTasks(filteredArchived);

        renderTasks(filteredActive, filteredArchived);
    }

    private boolean filterTask(tache t, String search, String priority) {

        boolean matchesSearch = search.isEmpty() ||
                t.getTitre().toLowerCase().contains(search);

        boolean matchesPriority = (priority == null || priority.equals("Toutes")) ||
                t.getPriorite().equalsIgnoreCase(mapPriorityToDB(priority));

        return matchesSearch && matchesPriority;
    }

    private String mapPriorityToDB(String uiPriority) {
        return switch (uiPriority.toLowerCase()) {
            case "haute" -> "ELEVEE";
            case "moyenne" -> "MOYEN";
            case "basse" -> "FAIBLE";
            default -> uiPriority;
        };
    }

    private void sortTasks(List<tache> list) {

        String sort = cmbSort.getValue();
        if (sort == null) return;

        switch (sort) {
            case "Date de création":
                list.sort(Comparator.comparing(tache::getDate_debut));
                break;
            case "Date d'échéance":
                list.sort(Comparator.comparing(tache::getDate_fin));
                break;
            case "Priorité":
                list.sort(Comparator.comparing(t -> t.getPriorite().toLowerCase()));
                break;
            case "Nom (A-Z)":
                list.sort(Comparator.comparing(t -> t.getTitre().toLowerCase()));
                break;
        }
    }

    private void renderTasks(List<tache> active, List<tache> archived) {

        activeTasksList.getItems().setAll(active);
        archivedTasksList.getItems().setAll(archived);

        lblActiveCount.setText(active.size() + " actives");
        lblArchivedCount.setText(archived.size() + " archivées");

        activeTasksList.setCellFactory(list -> new TaskCell(false));
        archivedTasksList.setCellFactory(list -> new TaskCell(true));
    }

    private class TaskCell extends ListCell<tache> {

        private final boolean archived;

        public TaskCell(boolean archived) {
            this.archived = archived;
        }

        @Override
        protected void updateItem(tache t, boolean empty) {
            super.updateItem(t, empty);

            if (empty || t == null) {
                setGraphic(null);
                return;
            }

            setGraphic(createTaskCard(t, archived));
        }
    }

    private HBox createTaskCard(tache t, boolean archived) {
        HBox root = new HBox(16);
        root.getStyleClass().addAll("task-item", archived ? "task-item-archived" : "task-item-active");
        root.setAlignment(Pos.CENTER_LEFT);

        // Priority Indicator
        StackPane priorityIndicator = new StackPane();
        String priorityClass = getPriorityClass(t.getPriorite());
        priorityIndicator.getStyleClass().addAll("task-priority-indicator", priorityClass);

        SVGPath priorityIcon = new SVGPath();
        priorityIcon.setContent(getPriorityIcon(t.getPriorite()));
        priorityIcon.getStyleClass().add("priority-icon");
        priorityIndicator.getChildren().add(priorityIcon);

        // Content
        VBox content = new VBox(8);
        HBox.setHgrow(content, Priority.ALWAYS);
        content.getStyleClass().add("task-content");

        Label title = new Label(t.getTitre());
        title.getStyleClass().add("task-title");

        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("task-description");
        desc.setWrapText(true);

        // Meta info
        HBox meta = new HBox(12);
        meta.getStyleClass().add("task-meta");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

        HBox dateBox = createMetaItem("M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z",
                sdf.format(t.getDate_debut()) + " → " + sdf.format(t.getDate_fin()), archived);

        meta.getChildren().add(dateBox);

        // Badges
        HBox badges = new HBox(8);
        badges.getStyleClass().add("badge-group");

        Label priorityBadge = new Label(t.getPriorite());
        priorityBadge.getStyleClass().addAll("task-badge", getPriorityBadgeClass(t.getPriorite()));

        Label statusBadge = new Label(t.getStatut());
        statusBadge.getStyleClass().addAll("task-badge", getStatusBadgeClass(t.getStatut()));

        badges.getChildren().addAll(priorityBadge, statusBadge);

        // ═══════════════════════════════════════════════════════════════
        // PREDICTION UI - COMPLETELY REDESIGNED
        // ═══════════════════════════════════════════════════════════════
        VBox predictionBox = new VBox(6);
        predictionBox.getStyleClass().add("prediction-box");

        if (t.getPrediction() > 0) {
            double completionProb = 1 - t.getPrediction();
            int percentage = (int)(completionProb * 100);

            // Determine status
            String statusClass;
            String statusText;
            String statusIcon;

            if (completionProb >= 0.7) {
                statusClass = "good";
                statusText = "Très probable";
                statusIcon = "✓";
            } else if (completionProb >= 0.4) {
                statusClass = "medium";
                statusText = "Incertain";
                statusIcon = "◐";
            } else {
                statusClass = "bad";
                statusText = "Risque élevé";
                statusIcon = "⚠";
            }

            // Top row: Label + Badge
            HBox topRow = new HBox(10);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label predictionLabel = new Label("Probabilité d'achèvement");
            predictionLabel.getStyleClass().add("prediction-label");

            Label badge = new Label(statusText);
            badge.getStyleClass().addAll("prediction-badge", statusClass);

            topRow.getChildren().addAll(predictionLabel, badge);

            // Middle row: Icon + ProgressBar + Percentage
            HBox barRow = new HBox(10);
            barRow.setAlignment(Pos.CENTER_LEFT);

            Label iconLabel = new Label(statusIcon);
            iconLabel.getStyleClass().addAll("prediction-icon", statusClass);

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefHeight(14);
            progressBar.setMaxHeight(14);
            progressBar.setPrefWidth(200);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(progressBar, Priority.ALWAYS);

            // THE KEY FIX: Add status class directly to ProgressBar for CSS targeting
            progressBar.getStyleClass().add("prediction-" + statusClass);

            Label percentLabel = new Label(percentage + "%");
            percentLabel.getStyleClass().addAll("prediction-percentage", statusClass);

            barRow.getChildren().addAll(iconLabel, progressBar, percentLabel);

            // Animation
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(progressBar.progressProperty(), 0, javafx.animation.Interpolator.EASE_OUT)),
                    new KeyFrame(Duration.millis(1000),
                            new KeyValue(progressBar.progressProperty(), completionProb, javafx.animation.Interpolator.EASE_OUT))
            );
            timeline.play();

            predictionBox.getChildren().addAll(topRow, barRow);

        } else {
            HBox unavailableRow = new HBox(8);
            unavailableRow.setAlignment(Pos.CENTER_LEFT);

            Label unavailableIcon = new Label("⊘");
            unavailableIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af;");

            Label unavailable = new Label("Prédiction indisponible");
            unavailable.getStyleClass().add("prediction-unavailable");

            unavailableRow.getChildren().addAll(unavailableIcon, unavailable);
            predictionBox.getChildren().add(unavailableRow);
        }

        content.getChildren().addAll(title, desc, meta, badges, predictionBox);
        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (!archived) {
            Button detailsBtn = new Button("Détails");
            detailsBtn.getStyleClass().addAll("task-btn", "task-btn-details");

            Button editBtn = new Button("Modifier");
            editBtn.getStyleClass().addAll("task-btn", "task-btn-edit");

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.getStyleClass().addAll("task-btn", "task-btn-delete");
            deleteBtn.setOnAction(e -> {
                try {
                    TacheService ts = new TacheService();
                    ts.delete(t.getId());
                    activeTasks.remove(t);
                    archivedTasks.remove(t);
                    refreshTasks();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("✔ Succès");
                    alert.setHeaderText("Tâche supprimée");
                    alert.setContentText("La tâche a été supprimée avec succès.");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("❌ Erreur");
                    alert.setHeaderText("Suppression échoué");
                    alert.setContentText("Erreur : " + ex.getMessage());
                    alert.showAndWait();
                }
            });
            // Details action
            detailsBtn.setOnAction(e -> {
                try {
                    if (sidebarController != null) {
                        // Set the task for the details controller to access
                        TachesController.setCurrentTaskForDetails(t);
                        sidebarController.loadView("/frontoffice/taches/tache-details.fxml");
                    }
                } catch (Exception ex) {
                    System.out.println("Error loading details: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // Edit action
            editBtn.setOnAction(e -> {
                try {
                    if (sidebarController != null) {
                        // Set the task for the edit controller to access
                        TachesController.setCurrentTaskForEdit(t);
                        sidebarController.loadView("/frontoffice/taches/tache-edit.fxml");
                    }
                } catch (Exception ex) {
                    System.out.println("Error loading edit: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });


            actions.getChildren().addAll(detailsBtn, editBtn, deleteBtn);
        } else {
            Button restoreBtn = new Button("Restaurer");
            restoreBtn.getStyleClass().addAll("task-btn", "task-btn-details");

            Button deletePermBtn = new Button("Supprimer");
            deletePermBtn.getStyleClass().addAll("task-btn", "task-btn-delete");

            restoreBtn.setOnAction(e -> {
                try {
                    t.setStatut("A_FAIRE");
                    TacheService ts = new TacheService();
                    ts.edit(t);
                    archivedTasks.remove(t);
                    activeTasks.add(t);
                    refreshTasks();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("✔ Succès");
                    alert.setHeaderText("Tâche restaurée");
                    alert.setContentText("La tâche a été restaurée avec succès.");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("❌ Erreur");
                    alert.setHeaderText("Restauration échouée");
                    alert.setContentText("Erreur : " + ex.getMessage());
                    alert.showAndWait();
                }
            });

            deletePermBtn.setOnAction(e -> {
                try {
                    TacheService ts = new TacheService();
                    ts.delete(t.getId());
                    allTasks.remove(t);
                    archivedTasks.remove(t);
                    refreshTasks();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("✔ Succès");
                    alert.setHeaderText("Tâche supprimée définitivement");
                    alert.setContentText("La tâche a été supprimée définitivement.");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("❌ Erreur");
                    alert.setHeaderText("Suppression échouée");
                    alert.setContentText("Erreur : " + ex.getMessage());
                    alert.showAndWait();
                }
            });

            actions.getChildren().addAll(restoreBtn, deletePermBtn);
        }

        root.getChildren().addAll(priorityIndicator, content, actions);
        return root;
    }

    private HBox createMetaItem(String iconContent, String text, boolean archived) {
        HBox box = new HBox(6);
        box.getStyleClass().addAll("task-meta-item", archived ? "task-meta-item-archived" : "task-meta-item-active");
        box.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = new SVGPath();
        icon.setContent(iconContent);
        icon.getStyleClass().add("task-meta-icon");

        Label label = new Label(text);
        label.getStyleClass().add("task-meta-text");

        box.getChildren().addAll(icon, label);
        return box;
    }

    private String getPriorityClass(String priority) {
        return switch (priority.toLowerCase()) {
            case "haute" -> "task-priority-high";
            case "moyenne" -> "task-priority-medium";
            case "basse" -> "task-priority-low";
            default -> "task-priority-low";
        };
    }

    private String getPriorityBadgeClass(String priority) {
        return switch (priority.toLowerCase()) {
            case "haute" -> "badge-priority-high";
            case "moyenne" -> "badge-priority-medium";
            case "basse" -> "badge-priority-low";
            default -> "badge-priority-low";
        };
    }

    private String getStatusBadgeClass(String status) {
        return switch (status.toLowerCase()) {
            case "a_faire", "todo" -> "badge-status-todo";
            case "en_cours", "in_progress" -> "badge-status-progress";
            case "terminee", "done" -> "badge-status-done";
            default -> "badge-status-archived";
        };
    }

    private String getPriorityIcon(String priority) {
        return switch (priority.toLowerCase()) {
            case "haute" ->
                    "M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z";
            case "moyenne" -> "M13 10V3L4 14h7v7l9-11h-7z";
            case "basse" -> "M5 13l4 4L19 7";
            default -> "M5 13l4 4L19 7";
        };
    }

    public void addTask(ActionEvent e) {
        if (sidebarController != null) {
            sidebarController.loadView("/frontoffice/taches/tache-new.fxml");
        } else {
            System.out.println("Sidebar controller is null, cannot load new task view");
        }
    }

    // Static methods to set tasks before loading views
    public static void setCurrentTaskForDetails(tache task) {
        currentTaskForDetails = task;
    }

    public static void setCurrentTaskForEdit(tache task) {
        currentTaskForEdit = task;
    }

    public static tache getCurrentTaskForEdit() {
        return currentTaskForEdit;
    }

    public static tache getCurrentTaskForDetails() {
        return currentTaskForDetails;
    }


    // WEATHER AI ADVICES
    @FXML
    public void handleWeather(ActionEvent e) {
        try {
            WeatherAiService.WeatherResult result = new WeatherAiService().getWeatherAndAdvice();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/taches/weather-response.fxml"));
            Parent root = loader.load();

            WeatherResponseController controller = loader.getController();
            if (result.error != null) {
                controller.setResponse(result.error);
            } else {
                controller.setWeatherData(result.description, result.temp, result.humidity, result.wind);
                controller.setAIAdvice(result.advice);
            }
            Stage stage = new Stage();
            stage.setTitle("Conseil météo & IA");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de la météo");
            alert.setContentText("Détails : " + ex.getMessage());
            alert.showAndWait();
        }

    }

    // BEHAVIOR ANALYSIS AI
    @FXML
    public void handleWeeklyAnalysis(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontoffice/taches/weekly-analysis.fxml")
            );

            Parent root = loader.load();

            BehaviorAnalysisController controller = loader.getController();
            controller.loadDataForUser(currentUserId);

            Stage stage = new Stage();
            stage.setTitle("Analyse hebdomadaire");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de l'analyse");
            alert.setContentText("Détails : " + ex.getMessage());
            alert.showAndWait();
        }
    }

    // STATS LOADING
    private void loadStatsAndCharts() {
        try {

            Map<String, Object> stats = statsService.getUserStats(currentUserId);
            Map<String, Integer> counts = (Map<String, Integer>) stats.get("counts");

            // UPDATE CARDS
            updateStatsCards(counts);

            // charts
            buildStatusChart(counts);
            buildPriorityChart(counts);
            buildProgressChart();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- STATUS CHART ----------------
    private void buildStatusChart(Map<String, Integer> c) {

        statusChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tâches");

        int todo = c.get("total")
                - c.get("completed")
                - c.get("abandoned")
                - c.get("inProgress")
                - c.get("overdue")
                - c.get("paused");

        series.getData().add(new XYChart.Data<>("À faire", todo));
        series.getData().add(new XYChart.Data<>("En cours", c.get("inProgress")));
        series.getData().add(new XYChart.Data<>("Terminées", c.get("completed")));
        series.getData().add(new XYChart.Data<>("Abandonnées", c.get("abandoned")));
        series.getData().add(new XYChart.Data<>("En retard", c.get("overdue")));
        series.getData().add(new XYChart.Data<>("En pause", c.get("paused")));

        statusChart.getData().add(series);
    }

    // ---------------- PRIORITY CHART ----------------
    private void buildPriorityChart(Map<String, Integer> c) {

        priorityChart.getData().clear();

        int high = c.getOrDefault("highPriority", 0);
        int remaining = Math.max(0, c.get("total") - high);

        int medium = (int) (remaining * 0.6);
        int low = remaining - medium;

        priorityChart.getData().add(new PieChart.Data("Haute", high));
        priorityChart.getData().add(new PieChart.Data("Moyenne", medium));
        priorityChart.getData().add(new PieChart.Data("Faible", low));
    }

    // ---------------- PROGRESS CHART (PLACEHOLDER) ----------------
    private void buildProgressChart() {

        progressChart.getData().clear();

        Map<String, Map<String, Integer>> data =
                statsService.getProgressLastDays(currentUserId, 30);

        XYChart.Series<String, Number> created = new XYChart.Series<>();
        created.setName("Créées");

        XYChart.Series<String, Number> completed = new XYChart.Series<>();
        completed.setName("Terminées");

        XYChart.Series<String, Number> abandoned = new XYChart.Series<>();
        abandoned.setName("Abandonnées");

        for (Map.Entry<String, Map<String, Integer>> entry : data.entrySet()) {

            String date = entry.getKey();
            Map<String, Integer> day = entry.getValue();

            created.getData().add(new XYChart.Data<>(date.substring(5), day.getOrDefault("created", 0)));
            completed.getData().add(new XYChart.Data<>(date.substring(5), day.getOrDefault("completed", 0)));
            abandoned.getData().add(new XYChart.Data<>(date.substring(5), day.getOrDefault("abandoned", 0)));
        }

        progressChart.getData().addAll(created, completed, abandoned);
    }

    private void updateStatsCards(Map<String, Integer> c) {

        int total = c.get("total");
        int completed = c.get("completed");
        int inProgress = c.get("inProgress");
        int overdue = c.get("overdue");

        lblTotalTasks.setText(String.valueOf(total));
        lblInProgress.setText(String.valueOf(inProgress));
        lblCompleted.setText(String.valueOf(completed));
        lblUrgent.setText(String.valueOf(overdue));
    }

    // NOTIFICATIONS
    private void setupNotificationsList() {
        // Add blur effect to main content when notifications are shown
        setupBlurEffect();

        notificationsList.setCellFactory(list -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setStyle(null);
                    return;
                }

                // Main card container with shadow and gradient border effect
                VBox root = new VBox(8);
                root.setStyle(
                        "-fx-padding: 14;"+
                                "-fx-background-color: white;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-radius: 12;" +
                                "-fx-border-color: linear-gradient(to right, #f8d5d5, #ffc4c4);" +
                                "-fx-border-width: 1.5;" +
                                "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.15), 8, 0, 0, 2);"+
                                "-fx-pref-width: 350;" + "-fx-max-width: 350;"
                );

                // Header with icon and message
                HBox header = new HBox(8);
                header.setAlignment(Pos.CENTER_LEFT);

                // Warning icon for overdue notifications
                Label icon = new Label("⚠");
                icon.setStyle("-fx-font-size: 16px;");

                Label message = new Label(item.getMessage());
                message.setWrapText(true);
                message.setMaxWidth(280);
                message.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2937;");

                // Bold if unread with accent color
                if (!item.isRead()) {
                    message.setStyle(
                            "-fx-font-size: 13px;" +
                                    "-fx-text-fill: #dc2626;" +
                                    "-fx-font-weight: bold;"
                    );
                    icon.setStyle("-fx-font-size: 16px; -fx-text-fill: #dc2626;");
                }

                header.getChildren().addAll(icon, message);

                // Date with icon
                HBox dateBox = new HBox(5);
                dateBox.setAlignment(Pos.CENTER_LEFT);
                Label dateIcon = new Label("🕐");
                dateIcon.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");

                Label date = new Label(
                        item.getCreatedAt() != null
                                ? item.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                                : ""
                );
                date.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
                dateBox.getChildren().addAll(dateIcon, date);

                // Action buttons with gradient styling
                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);
                actions.setPadding(new Insets(5, 0, 0, 0));

                Button btnFinish = new Button("✓ Terminer");
                btnFinish.setStyle(
                        "-fx-background-color: linear-gradient(to right, #16a34a, #15803d);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 14;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(22, 163, 74, 0.3), 4, 0, 0, 1);"
                );

                // Hover effect
                btnFinish.setOnMouseEntered(e -> btnFinish.setStyle(
                        "-fx-background-color: linear-gradient(to right, #15803d, #166534);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 14;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(22, 163, 74, 0.4), 6, 0, 0, 2);"
                ));
                btnFinish.setOnMouseExited(e -> btnFinish.setStyle(
                        "-fx-background-color: linear-gradient(to right, #16a34a, #15803d);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 14;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(22, 163, 74, 0.3), 4, 0, 0, 1);"
                ));

                Button btnAbandon = new Button("✕ Abandonner");
                btnAbandon.setStyle(
                        "-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 14;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.3), 4, 0, 0, 1);"
                );

                // Hover effect for abandon
                btnAbandon.setOnMouseEntered(e -> btnAbandon.setStyle(
                        "-fx-background-color: linear-gradient(to right, #b91c1c, #991b1b);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 14;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.4), 6, 0, 0, 2);"
                ));
                btnAbandon.setOnMouseExited(e -> btnAbandon.setStyle(
                        "-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 14;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.3), 4, 0, 0, 1);"
                ));

                actions.getChildren().addAll(btnFinish, btnAbandon);

                // Set actions
                btnFinish.setOnAction(e -> handleNotificationAction(item, "finish"));
                btnAbandon.setOnAction(e -> handleNotificationAction(item, "abandon"));

                root.getChildren().addAll(header, dateBox, actions);
                setGraphic(root);

                // Add spacing between cells
                setStyle("-fx-padding: 0 0 10 0; -fx-background-color: transparent;");
            }
        });

        // Improve the list view itself
        notificationsList.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 10;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 0;"
        );

        // Remove default list view borders and make it fill width
        notificationsList.setPrefWidth(450);
        notificationsList.setMaxWidth(450);
    }

    // Method to setup blur effect on main content
    private void setupBlurEffect() {
        if (mainContent == null) return;

        // Create the blur effect
        GaussianBlur blur = new GaussianBlur(0);

        // Apply to main content when notifications show
        notificationArea.visibleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Show notifications - apply blur
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
                        new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 10))
                );
                timeline.play();
                mainContent.setEffect(blur);
                mainContent.setMouseTransparent(true); // Prevent interaction with blurred content
            } else {
                // Hide notifications - remove blur
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 10)),
                        new KeyFrame(Duration.millis(200), new KeyValue(blur.radiusProperty(), 0))
                );
                timeline.setOnFinished(e -> {
                    mainContent.setEffect(null);
                    mainContent.setMouseTransparent(false);
                });
                timeline.play();
            }
        });
    }

    private void handleNotificationAction(Notification notif, String action) {
        try {
            if (notif.getTacheId() == null) return;

            // Get task
            tache t = ts.getById(notif.getTacheId()); // make sure this exists

            if (t == null) return;

            String currentStatus = t.getStatut();
            String newStatus = "";

            switch (action) {
                case "finish":
                    newStatus = "TERMINE";
                    break;
                case "abandon":
                    newStatus = "ABANDON";
                    break;
            }

            if (!newStatus.equals(currentStatus)) {

                // update task
                t.setStatut(newStatus);
                t.setUpdated_at(new Date());
                ts.edit(t);

                // mark notification as read
                notif.setRead(true);
                nss.edit(notif); // create this if not exists

                refreshTasks();
                loadNotifications();

            }

        } catch (Exception e) {
            System.out.println("Action notif error: " + e.getMessage());
        }
    }
    @FXML
    private void toggleNotifications(javafx.scene.input.MouseEvent event) {
        notificationsVisible = !notificationsVisible;
        notificationArea.setVisible(notificationsVisible);
        notificationArea.setManaged(notificationsVisible);

        if (notificationsVisible) {
            loadNotifications(); // refresh when opening
        }
        event.consume();
    }

    private void loadNotifications() {
        System.out.println("LOAD NOTIFICATIONS CALLED");

        notifScheduler.submit(() -> {
            try {
                ns.runForUser(currentUser);

                List<Notification> allNotifs = ns.getUserNotifications(currentUserId);
                List<Notification> unreadNotifs = allNotifs.stream()
                        .filter(n -> !n.isRead())
                        .toList();

                System.out.println("FETCHED UNREAD NOTIFS = " + unreadNotifs.size());

                javafx.application.Platform.runLater(() -> {
                    notificationsList.getItems().setAll(unreadNotifs);

                    // Show dot if there are unread notifications
                    notificationDot.setVisible(!unreadNotifs.isEmpty());
                });

            } catch (Exception e) {
                System.out.println("Load notif error: " + e.getMessage());
            }
        });
    }

    private void startNotificationAutoRefresh() {
        loadNotifications(); // initial load

        notifScheduler.scheduleAtFixedRate(() -> {
            loadNotifications();
        }, 30, 30, TimeUnit.SECONDS);
    }

    private String getPredictionBarClass(double prob) {
        if (prob >= 0.7) return "prediction-good";
        if (prob >= 0.4) return "prediction-medium";
        return "prediction-bad";
    }
}