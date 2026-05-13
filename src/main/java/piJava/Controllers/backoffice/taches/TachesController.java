package piJava.Controllers.backoffice.taches;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
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

    // ===== FXML INJECTIONS (Updated for new layout) =====
    @FXML private TableView<tache> tasksTableView;
    @FXML private TableColumn<tache, Integer> colId;
    @FXML private TableColumn<tache, String> colTitre;
    @FXML private TableColumn<tache, String> colType;
    @FXML private TableColumn<tache, String> colDates;
    @FXML private TableColumn<tache, String> colPriorite;
    @FXML private TableColumn<tache, String> colStatut;
    @FXML private TableColumn<tache, String> colUser;
    @FXML private TableColumn<tache, Void> colActions;

    @FXML private Label lblTotalCount;
    @FXML private Label lblActiveCount;
    @FXML private Label lblUrgentCount;
    @FXML private Label lblCompletedCount;
    @FXML private Label lblTaskCount;
    @FXML private Label lblUserInfo;
    @FXML private Label lblFooter;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbPriority;
    @FXML private ComboBox<String> cmbSort;
    @FXML private Button btnResetFilters;

    private ObservableList<tache> tableItems = FXCollections.observableArrayList();

    // MASTER DATA (IMPORTANT - keep all logic intact)
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

            // Update stat counts
            updateStats();

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

            // Setup TableView columns
            setupTableColumns();

            // listeners
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            cmbPriority.setOnAction(e -> applyFilters());
            cmbSort.setOnAction(e -> applyFilters());

            btnResetFilters.setOnAction(e -> resetFilters(null));

            // initial load
            applyFilters();

            // Set user info if available
            lblUserInfo.setText("Connecté en tant que: Admin");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        int total = allTasks.size();
        int active = (int) allTasks.stream()
                .filter(t -> t.getStatut() != null &&
                        (t.getStatut().equalsIgnoreCase("A_FAIRE") ||
                                t.getStatut().equalsIgnoreCase("EN_COURS")))
                .count();
        int urgent = (int) allTasks.stream()
                .filter(t -> t.getPriorite() != null &&
                        (t.getPriorite().equalsIgnoreCase("ELEVEE") ||
                                t.getPriorite().equalsIgnoreCase("Haute")))
                .count();
        int completed = (int) allTasks.stream()
                .filter(t -> t.getStatut() != null &&
                        (t.getStatut().equalsIgnoreCase("TERMINEE") ||
                                t.getStatut().equalsIgnoreCase("Terminé")))
                .count();

        lblTotalCount.setText(String.valueOf(total));
        lblActiveCount.setText(String.valueOf(active));
        lblUrgentCount.setText(String.valueOf(urgent));
        lblCompletedCount.setText(String.valueOf(completed));
    }

    private void setupTableColumns() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // ID Column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: CENTER;");

        // Titre Column
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));

        // Type Column with badge
        colType.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType();
            return new SimpleStringProperty(type != null ? type : "-");
        });
        colType.setCellFactory(column -> new TableCell<tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add(getTypeBadgeClass(item));
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Dates Column
        colDates.setCellValueFactory(cellData -> {
            tache t = cellData.getValue();
            String debut = t.getDate_debut() != null ? sdf.format(t.getDate_debut()) : "-";
            String fin = t.getDate_fin() != null ? sdf.format(t.getDate_fin()) : "-";
            return new SimpleStringProperty(debut + " → " + fin);
        });

        // Priorité Column with badge
        colPriorite.setCellValueFactory(cellData -> {
            String prio = cellData.getValue().getPriorite();
            return new SimpleStringProperty(normalizePriority(prio));
        });
        colPriorite.setCellFactory(column -> new TableCell<tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add(getPriorityBadgeClass(item));
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Statut Column with badge
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add(getStatusBadgeClass(item));
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // User Column with avatar
        colUser.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getUser_id();
            return new SimpleStringProperty("User #" + userId);
        });
        colUser.setCellFactory(column -> new TableCell<tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label avatar = new Label(item.replace("User #", ""));
                    avatar.getStyleClass().add("user-avatar-small");
                    avatar.setMinSize(32, 32);
                    avatar.setMaxSize(32, 32);
                    avatar.setAlignment(Pos.CENTER);

                    Label name = new Label(item);
                    name.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");

                    box.getChildren().addAll(avatar, name);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Actions Column
        colActions.setCellFactory(param -> new TableCell<tache, Void>() {
            private final Button details = new Button("👁");
            private final Button edit = new Button("✏");
            private final Button delete = new Button("🗑");
            private final HBox pane = new HBox(6, details, edit, delete);

            {
                pane.setAlignment(Pos.CENTER);

                details.getStyleClass().addAll("action-btn", "btn-view");
                edit.getStyleClass().addAll("action-btn", "btn-edit");
                delete.getStyleClass().addAll("action-btn", "btn-delete");

                details.setTooltip(new Tooltip("Voir détails"));
                edit.setTooltip(new Tooltip("Modifier"));
                delete.setTooltip(new Tooltip("Supprimer"));

                details.setOnAction(e -> {
                    tache t = getTableView().getItems().get(getIndex());
                    TachesController.setCurrentTaskForDetails(t);
                    sidebarController.loadView("/backoffice/taches/tache-details.fxml");
                });

                edit.setOnAction(e -> {
                    tache t = getTableView().getItems().get(getIndex());
                    TachesController.setCurrentTaskForEdit(t);
                    sidebarController.loadView("/backoffice/taches/tache-edit.fxml");
                });

                delete.setOnAction(e -> {
                    tache t = getTableView().getItems().get(getIndex());
                    handleDelete(t, getTableView().getItems());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void handleDelete(tache t, ObservableList<tache> items) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer cette tâche ?");
        confirm.setContentText("Cette action est irréversible et supprimera définitivement la tâche.");

        // Style the dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/backoffice/taches/tache.css").toExternalForm());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                TacheService ts = new TacheService();
                ts.delete(t.getId());

                // Animate removal
                items.remove(t);
                allTasks.remove(t);

                // Update stats and counts
                updateStats();
                int count = items.size();
                lblTaskCount.setText(count + " tâche" + (count > 1 ? "s" : "") + " trouvée" + (count > 1 ? "s" : ""));
                lblFooter.setText("© 2026 ESPRIT École d'Ingénieurs. Gestion des Tâches - " + count + " tâches | " + lblActiveCount.getText() + " en cours");

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("✔ Succès");
                success.setHeaderText("Tâche supprimée");
                success.setContentText("La tâche a été supprimée avec succès.");
                success.showAndWait();

            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("❌ Erreur");
                alert.setHeaderText("Suppression échouée");
                alert.setContentText("Erreur : " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    private String getPriorityBadgeClass(String priority) {
        if (priority == null) return "badge-priority-low";
        return switch (priority.toUpperCase()) {
            case "ELEVEE", "HAUTE", "URGENT" -> "badge-priority-high";
            case "MOYEN", "MOYENNE", "MEDIUM" -> "badge-priority-medium";
            case "FAIBLE", "BASSE", "LOW" -> "badge-priority-low";
            default -> "badge-priority-low";
        };
    }

    private String getStatusBadgeClass(String status) {
        if (status == null) return "badge-status-pending";
        return switch (status.toUpperCase()) {
            case "A_FAIRE", "A FAIRE", "PENDING", "À FAIRE" -> "badge-status-pending";
            case "EN_COURS", "EN COURS", "INPROGRESS", "IN_PROGRESS" -> "badge-status-inprogress";
            case "TERMINEE", "TERMINE", "COMPLETED", "DONE", "TERMINÉ" -> "badge-status-completed";
            case "PAUSED", "EN_PAUSE", "EN PAUSE" -> "badge-status-paused";
            default -> "badge-status-pending";
        };
    }

    private String getTypeBadgeClass(String type) {
        if (type == null) return "badge-type-manuel";
        return switch (type.toUpperCase()) {
            case "MANUEL" -> "badge-type-manuel";
            case "REUNION", "RÉUNION" -> "badge-type-reunion";
            case "REVISION", "RÉVISION" -> "badge-type-revision";
            case "SANTE", "SANTÉ" -> "badge-type-sante";
            case "EMPLOI" -> "badge-type-emploi";
            default -> "badge-type-manuel";
        };
    }

    public void addTask(ActionEvent e) {
        sidebarController.loadView("/backoffice/taches/tache-new.fxml");
    }

    // Apply button handler
    public void applyFilters(ActionEvent e) {
        applyFilters();
    }

    // CORE FILTER ENGINE (Keep logic exactly the same)
    private void applyFilters() {

        String search = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
        String priority = cmbPriority.getValue();
        String sort = cmbSort.getValue();

        List<tache> filtered = allTasks.stream()
                // SEARCH
                .filter(t -> search.isEmpty()
                        || (t.getTitre() != null && t.getTitre().toLowerCase().contains(search)))
                .filter(t -> priority == null
                        || priority.equals("Toutes")
                        || normalizePriority(t.getPriorite()).equalsIgnoreCase(priority))
                .collect(Collectors.toList());

        // SORTING
        if (sort != null) {
            switch (sort) {
                case "Nom (A-Z)" ->
                        filtered.sort(Comparator.comparing(t -> t.getTitre() != null ? t.getTitre().toLowerCase() : ""));

                case "Date début" ->
                        filtered.sort(Comparator.comparing(t -> t.getDate_debut(), Comparator.nullsLast(Date::compareTo)));

                case "Date fin" ->
                        filtered.sort(Comparator.comparing(t -> t.getDate_fin(), Comparator.nullsLast(Date::compareTo)));

                case "Priorité" ->
                        filtered.sort(Comparator.comparing(t -> priorityWeight(t.getPriorite())));
            }
        }

        // Update table
        tableItems.clear();
        tableItems.addAll(filtered);
        tasksTableView.setItems(tableItems);

        // Update count labels
        int count = filtered.size();
        lblTaskCount.setText(count + " tâche" + (count > 1 ? "s" : "") + " trouvée" + (count > 1 ? "s" : ""));
        lblFooter.setText("© 2026 ESPRIT École d'Ingénieurs. Gestion des Tâches - " + count + " tâches | " + lblActiveCount.getText() + " en cours");
    }

    // RESET
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