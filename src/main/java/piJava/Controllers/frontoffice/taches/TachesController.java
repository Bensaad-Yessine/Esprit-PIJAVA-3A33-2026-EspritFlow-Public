package piJava.Controllers.frontoffice.taches;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import piJava.Controllers.frontoffice.taches.TacheEditController;
import piJava.Controllers.frontoffice.taches.TachesDetailsController;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.services.TacheService;
import piJava.entities.tache;
import piJava.services.api.BehaviorAnalysisService;
import piJava.services.api.WeatherAiService;
import piJava.utils.SessionManager;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TachesController implements Initializable {

    private final int currentUserId = SessionManager.getInstance().getCurrentUser().getId();

    @FXML
    private ListView<tache> activeTasksList;
    @FXML
    private ListView<tache> archivedTasksList;

    @FXML
    private Label lblActiveCount;
    @FXML
    private Label lblArchivedCount;
    @FXML
    private Label notificationLabel;

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbPriority;
    @FXML
    private ComboBox<String> cmbSort;
    @FXML
    private Button btnResetFilters;
    @FXML
    private Button btnWeather;
    @FXML
    private Button btnWeeklyAnalysis;
    private List<tache> allTasks = new ArrayList<>();
    private List<tache> activeTasks = new ArrayList<>();
    private List<tache> archivedTasks = new ArrayList<>();

    private FrontSidebarController sidebarController;

    // Static variables to temporarily store task data
    private static tache currentTaskForDetails;
    private static tache currentTaskForEdit;

    public void setSidebarController(FrontSidebarController sidebarcontroller) {
        this.sidebarController = sidebarcontroller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadTasks();

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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() throws SQLException {
        TacheService ts = new TacheService();

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

        content.getChildren().addAll(title, desc, meta, badges);

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

}