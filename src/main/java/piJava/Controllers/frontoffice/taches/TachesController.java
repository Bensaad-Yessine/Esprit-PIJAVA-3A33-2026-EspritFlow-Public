package piJava.Controllers.frontoffice.taches;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.tache;
import piJava.services.TacheService;
import piJava.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class TachesController implements Initializable {

    // ✅ Comes from session instead of hardcoded
    private int currentUserId;

    @FXML private VBox activeTasksContainer;
    @FXML private VBox archivedTasksContainer;
    @FXML private Label lblActiveCount;
    @FXML private Label lblArchivedCount;
    @FXML private Label notificationLabel;

    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ✅ Get logged-in user id from session
        if (SessionManager.getInstance().isLoggedIn()) {
            currentUserId = SessionManager.getInstance().getCurrentUser().getId();
        } else {
            showError("Session expirée", "Veuillez vous reconnecter.");
            return;
        }

        loadTasks();
    }

    // ── Load & render tasks ────────────────────────────────────────────────────

    private void loadTasks() {
        try {
            TacheService ts = new TacheService();
            List<tache> allTasks = ts.showUserTasks(currentUserId);

            List<String> activeStatuses = Arrays.asList("EN_COURS", "A_FAIRE", "PAUSED");
            List<tache> activeTasks   = new ArrayList<>();
            List<tache> archivedTasks = new ArrayList<>();

            for (tache t : allTasks) {
                if (activeStatuses.contains(t.getStatut())) activeTasks.add(t);
                else archivedTasks.add(t);
            }

            activeTasksContainer.getChildren().clear();
            archivedTasksContainer.getChildren().clear();

            for (tache t : activeTasks) {
                activeTasksContainer.getChildren().add(createTaskCard(t, false));
            }
            for (tache t : archivedTasks) {
                archivedTasksContainer.getChildren().add(createTaskCard(t, true));
            }

            lblActiveCount.setText(activeTasks.size() + " actives");
            lblArchivedCount.setText(archivedTasks.size() + " archivées");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger les tâches.\n" + e.getMessage());
        }
    }

    // ── Build a task card ──────────────────────────────────────────────────────

    private HBox createTaskCard(tache t, boolean archived) {
        HBox root = new HBox(20);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add(archived ? "archived-task-item" : "task-item");

        // Priority bar
        VBox priorityBar = new VBox();
        priorityBar.setPrefWidth(8);
        String priorityClass = t.getPriorite() != null ? t.getPriorite().toLowerCase() : "basse";
        priorityBar.getStyleClass().add(
                archived ? "archived-priority-bar-" + priorityClass
                        : "priority-bar-" + priorityClass
        );

        // Content
        VBox content = new VBox(10);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Top row: title + badges
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(t.getTitre());
        title.getStyleClass().add(archived ? "archived-task-title" : "task-title");

        Label priorityBadge = new Label(t.getPriorite());
        priorityBadge.getStyleClass().add(
                archived ? "archived-priority-badge-" + priorityClass
                        : "priority-badge-" + priorityClass
        );

        String statusClass = t.getStatut() != null
                ? t.getStatut().toLowerCase().replace(" ", "-") : "inconnu";
        Label statusBadge = new Label(t.getStatut());
        statusBadge.getStyleClass().add(
                archived ? "archived-status-badge-" + statusClass
                        : "status-badge-" + statusClass
        );

        topRow.getChildren().addAll(title, priorityBadge, statusBadge);

        // Description
        Label desc = new Label(t.getDescription() != null ? t.getDescription() : "");
        desc.setWrapText(true);
        desc.getStyleClass().add(archived ? "archived-task-description" : "task-description");

        // Dates meta
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String debut = t.getDate_debut() != null ? sdf.format(t.getDate_debut()) : "—";
        String fin   = t.getDate_fin()   != null ? sdf.format(t.getDate_fin())   : "—";
        Label dates = new Label("📅 Début: " + debut + "  |  Fin: " + fin);
        dates.getStyleClass().add(archived ? "archived-task-meta" : "task-meta");

        content.getChildren().addAll(topRow, desc, dates);

        // Action buttons
        VBox actions = new VBox(10);
        actions.setAlignment(Pos.CENTER);

        Button btnDetails = new Button("📄 Détails");
        Button btnEdit    = new Button(archived ? "↩️ Restaurer" : "✏️ Modifier");
        Button btnDelete  = new Button("🗑 Supprimer");

        btnDetails.getStyleClass().add(archived ? "btn-details-archived" : "btn-details");
        btnEdit.getStyleClass().add(archived ? "btn-restore" : "btn-edit");
        btnDelete.getStyleClass().add(archived ? "btn-delete-archived" : "btn-delete");

        // ── Delete ──────────────────────────────────────────────
        btnDelete.setOnAction(e -> handleDelete(t, root, archived));

        // ── Details ─────────────────────────────────────────────
        btnDetails.setOnAction(e -> handleDetails(t));

        // ── Edit ────────────────────────────────────────────────
        btnEdit.setOnAction(e -> handleEdit(t, archived));

        actions.getChildren().addAll(btnDetails, btnEdit, btnDelete);
        root.getChildren().addAll(priorityBar, content, actions);
        return root;
    }

    // ── Button handlers ────────────────────────────────────────────────────────

    private void handleDelete(tache t, HBox card, boolean archived) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer \"" + t.getTitre() + "\" ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    new TacheService().delete(t.getId());
                    ((VBox) card.getParent()).getChildren().remove(card);

                    if (archived) {
                        int count = parseCount(lblArchivedCount.getText()) - 1;
                        lblArchivedCount.setText(count + " archivées");
                    } else {
                        int count = parseCount(lblActiveCount.getText()) - 1;
                        lblActiveCount.setText(count + " actives");
                    }

                } catch (SQLException ex) {
                    showError("Suppression échouée", ex.getMessage());
                }
            }
        });
    }

    private void handleDetails(tache t) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontoffice/taches/tache-details.fxml"));
            Parent r = loader.load();

            TachesDetailsController controller = loader.getController();
            controller.setTask(t);
            controller.setSidebarController(sidebarController);

            sidebarController.getContentArea().getChildren().setAll(r);
            sidebarController.setActivePage("taches");

        } catch (IOException ex) {
            showError("Erreur de navigation", ex.getMessage());
        }
    }

    private void handleEdit(tache t, boolean archived) {
        if (archived) {
            // Restore: set status back to A_FAIRE and save
            try {
                t.setStatut("A_FAIRE");
                new TacheService().edit(t);
                loadTasks(); // Refresh the whole list
            } catch (SQLException ex) {
                showError("Restauration échouée", ex.getMessage());
            }
        } else {
            // Navigate to edit form
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/frontoffice/taches/tache-edit.fxml"));
                Parent r = loader.load();

                TacheEditController controller = loader.getController();
                controller.setTask(t);
                controller.setSidebarController(sidebarController);

                sidebarController.getContentArea().getChildren().setAll(r);
                sidebarController.setActivePage("taches");

            } catch (IOException ex) {
                showError("Erreur de navigation", ex.getMessage());
            }
        }
    }

    @FXML
    public void addTask(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontoffice/taches/tache-new.fxml"));
            Parent r = loader.load();

            TacheNewController controller = loader.getController();
            controller.setSidebarController(sidebarController);

            sidebarController.getContentArea().getChildren().setAll(r);
            sidebarController.setActivePage("taches");

        } catch (IOException ex) {
            showError("Erreur de navigation", ex.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private int parseCount(String text) {
        try { return Integer.parseInt(text.split(" ")[0]); }
        catch (NumberFormatException e) { return 0; }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
