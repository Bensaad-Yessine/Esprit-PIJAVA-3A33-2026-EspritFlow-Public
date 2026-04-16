package piJava.Controllers.backoffice.taches;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import piJava.Controllers.backoffice.SidebarController;
import piJava.Controllers.backoffice.taches.TacheEditController;
import piJava.Controllers.backoffice.taches.TacheNewController;
import piJava.Controllers.backoffice.taches.TachesDetailsController;
import piJava.entities.tache;
import piJava.services.TacheService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TachesController implements Initializable {

    @FXML private ListView<HBox> tasksListView;
    @FXML private Label lblActiveCount;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbPriority;
    @FXML private ComboBox<String> cmbSort;
    @FXML private Button btnResetFilters;

    private ObservableList<HBox> items = FXCollections.observableArrayList();

    // 🔥 MASTER DATA (IMPORTANT)
    private List<tache> allTasks = new ArrayList<>();

    private SidebarController sidebarController;

    // Static variables to temporarily store task data
    private static tache currentTaskForDetails;
    private static tache currentTaskForEdit;

    public void setSidebarController(SidebarController sidebarcontroller) {
        this.sidebarController = sidebarcontroller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            TacheService ts = new TacheService();
            allTasks = ts.show();

            // init filters
            cmbPriority.setItems(FXCollections.observableArrayList(
                    "Toutes", "Haute", "Moyenne", "Basse"
            ));
            cmbPriority.getSelectionModel().select("Toutes");

            cmbSort.setItems(FXCollections.observableArrayList(
                    "Nom (A-Z)",
                    "Nom (Z-A)",
                    "Date début",
                    "Date fin",
                    "Priorité"
            ));
            cmbSort.getSelectionModel().select("Nom (A-Z)");

            // listeners
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            cmbPriority.setOnAction(e -> applyFilters());
            cmbSort.setOnAction(e -> applyFilters());

            btnResetFilters.setOnAction(e -> resetFilters(null));

            // initial load
            applyFilters();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createTaskCard(tache t) {
        HBox root = new HBox(14);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("task-card");
        root.setStyle("-fx-fill-width: true; -fx-max-width: Infinity;");

        // ─── Priority bar ───────────────────────────────
        VBox priorityBar = new VBox();
        priorityBar.setPrefWidth(5);
        priorityBar.setMinHeight(80);

        String priorityClass = t.getPriorite().toLowerCase(); // haute, moyenne, basse
        priorityBar.getStyleClass().add("priority-" + priorityClass);

        // ─── Content ───────────────────────────────
        VBox content = new VBox(10);
        HBox.setHgrow(content, Priority.ALWAYS);
        content.setStyle("-fx-fill-width: true;");

        // Top row - Title + Badges
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(t.getTitre());
        title.getStyleClass().add("task-title");
        title.setStyle("-fx-font-size: 15px;");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label priority = new Label(normalizePriority(t.getPriorite()));        priority.getStyleClass().add("badge-priority");

        Label status = new Label(t.getStatut());
        status.getStyleClass().add("badge-status");

        topRow.getChildren().addAll(title, priority, status);

        // Description
        Label desc = new Label(t.getDescription() != null ? t.getDescription() : "No description");
        desc.setWrapText(true);
        desc.getStyleClass().add("task-desc");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #D1D5DB;");

        // Dates + User info in a single row
        HBox metaRow = new HBox(24);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String debut = t.getDate_debut() != null ? sdf.format(t.getDate_debut()) : "-";
        String fin = t.getDate_fin() != null ? sdf.format(t.getDate_fin()) : "-";

        Label dates = new Label("📅 " + debut + " → " + fin);
        dates.getStyleClass().add("task-meta");
        dates.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        Label userInfo = new Label("👤 User #" + t.getUser_id());
        userInfo.getStyleClass().add("task-user");
        userInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #60A5FA;");

        metaRow.getChildren().addAll(dates, userInfo);

        content.getChildren().addAll(topRow, desc, metaRow);

        // ─── Actions ─────────────────────────────
        VBox actions = new VBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setStyle("-fx-padding: 0 0 0 8;");

        Button details = new Button("👁");
        Button edit = new Button("✏");
        Button delete = new Button("🗑");

        details.getStyleClass().add("btn-details");
        edit.getStyleClass().add("btn-edit");
        delete.getStyleClass().add("btn-delete");

        details.setPrefWidth(44);
        details.setPrefHeight(44);
        edit.setPrefWidth(44);
        edit.setPrefHeight(44);
        delete.setPrefWidth(44);
        delete.setPrefHeight(44);

        // Tooltip for accessibility
        details.setTooltip(new Tooltip("Voir détails"));
        edit.setTooltip(new Tooltip("Modifier"));
        delete.setTooltip(new Tooltip("Supprimer"));
        delete.setOnAction(e -> {
            try {
                TacheService ts = new TacheService();
                ts.delete(t.getId());

                // remove from ListView properly
                items.remove(root);

                // update counter safely
                int count = Integer.parseInt(lblActiveCount.getText().split(" ")[0]) - 1;
                lblActiveCount.setText(count + " tâches");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("✔ Succès");
                alert.setHeaderText("Tâche supprimée");
                alert.setContentText("La tâche a été supprimée avec succès.");
                alert.showAndWait();

            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("❌ Erreur");
                alert.setHeaderText("Suppression échouée");
                alert.setContentText("Erreur : " + ex.getMessage());
                alert.showAndWait();
            }
        });
        // Details action
        details.setOnAction(e -> {
            TachesController.setCurrentTaskForDetails(t);
            sidebarController.loadView("/backoffice/taches/tache-details.fxml");
        });

        // Edit action
        edit.setOnAction(e -> {
            TachesController.setCurrentTaskForEdit(t);
            sidebarController.loadView("/backoffice/taches/tache-edit.fxml");
        });

        actions.getChildren().addAll(details, edit, delete);

        root.getChildren().addAll(priorityBar, content, actions);

        return root;
    }
    public void addTask(ActionEvent e) {
        sidebarController.loadView("/backoffice/taches/tache-new.fxml");
    }


    // 🔥 CORE FILTER ENGINE
    private void applyFilters() {

        String search = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
        String priority = cmbPriority.getValue();
        String sort = cmbSort.getValue();

        List<tache> filtered = allTasks.stream()

                // SEARCH
                .filter(t -> search.isEmpty()
                        || t.getTitre().toLowerCase().contains(search))

                .filter(t -> priority == null
                        || priority.equals("Toutes")
                        || normalizePriority(t.getPriorite()).equalsIgnoreCase(priority))
                .collect(Collectors.toList());

        // SORTING
        if (sort != null) {
            switch (sort) {
                case "Nom (A-Z)" ->
                        filtered.sort(Comparator.comparing(t -> t.getTitre().toLowerCase()));

                case "Date début" ->
                        filtered.sort(Comparator.comparing(t -> t.getDate_debut(), Comparator.nullsLast(Date::compareTo)));

                case "Date fin" ->
                        filtered.sort(Comparator.comparing(t -> t.getDate_fin(), Comparator.nullsLast(Date::compareTo)));

                case "Priorité" ->
                        filtered.sort(Comparator.comparing(t -> priorityWeight(t.getPriorite())));
            }
        }

        // rebuild UI
        items.clear();
        for (tache t : filtered) {
            items.add(createTaskCard(t));
        }

        tasksListView.setItems(items);
        lblActiveCount.setText(filtered.size() + " tâches au total");
    }
    // 🔄 RESET
    public void resetFilters(ActionEvent e) {
        txtSearch.clear();
        cmbPriority.getSelectionModel().select("Toutes");
        cmbSort.getSelectionModel().select("Nom (A-Z)");
        applyFilters();
    }
    private int priorityWeight(String p) {
        if (p == null) return 0;

        return switch (p.toUpperCase()) {
            case "ELEVEE" -> 3;
            case "MOYEN" -> 2;
            case "FAIBLE" -> 1;
            default -> 0;
        };
    }
    private String normalizePriority(String p) {
        if (p == null) return "";

        return switch (p.toUpperCase()) {
            case "ELEVEE" -> "Haute";
            case "MOYEN" -> "Moyenne";
            case "FAIBLE" -> "Basse";
            default -> p;
        };
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
}
