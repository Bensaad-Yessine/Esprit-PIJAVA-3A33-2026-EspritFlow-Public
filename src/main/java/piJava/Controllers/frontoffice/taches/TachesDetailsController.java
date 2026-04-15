package piJava.Controllers.frontoffice.taches;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.tache;
import piJava.entities.suiviTache;
import piJava.services.SuiviTacheService;
import piJava.services.TacheService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TachesDetailsController {

    @FXML private Label lblTitle;
    @FXML private Label lblDescription;
    @FXML private Label lblStatus;
    @FXML private Label lblPriority;
    @FXML private Label lblType;
    @FXML private Label lblDateDebut;
    @FXML private Label lblDateFin;
    @FXML private Label lblDateEcheance;
    @FXML private Label lblDureeEstimee;
    @FXML private Label lblPrediction;
    @FXML private ProgressBar progressPrediction;
    @FXML private ListView<String> lvHistory;
    @FXML private Button btnBack;
    @FXML private Button btnStart;
    @FXML private Button btnPause;
    @FXML private Button btnFinish;
    @FXML private Button btnAbandon;
    @FXML private Button btnNoActions;

    private tache task;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setTask(tache task) {
        this.task = task;
        populateTaskDetails();
        loadHistory();
        updateActionButtons();
    }

    // ── Populate ───────────────────────────────────────────────────────────────

    private void populateTaskDetails() {
        lblTitle.setText(task.getTitre());
        lblDescription.setText(task.getDescription());
        lblType.setText(task.getType());

        lblStatus.setText(task.getStatut());
        styleStatusLabel(task.getStatut());

        lblPriority.setText(task.getPriorite());
        stylePriorityLabel(task.getPriorite());

        lblDateDebut.setText(task.getDate_debut() != null ? dateFormat.format(task.getDate_debut()) : "Non définie");
        lblDateFin.setText(task.getDate_fin() != null ? dateFormat.format(task.getDate_fin()) : "Non définie");
        lblDateEcheance.setText(task.getDate_echeance() != null ? dateFormat.format(task.getDate_echeance()) : "Non définie");

        lblDureeEstimee.setText(task.getDuree_estimee() > 0 ? task.getDuree_estimee() + " heures" : "Non estimée");

        if (task.getPrediction() > 0) {
            lblPrediction.setText(String.format("%.1f%%", task.getPrediction() * 100));
            progressPrediction.setProgress(task.getPrediction());
            stylePredictionProgress(task.getPrediction());
        } else {
            lblPrediction.setText("Non disponible");
            progressPrediction.setProgress(0);
        }
    }

    private void styleStatusLabel(String statut) {
        lblStatus.getStyleClass().removeAll("status-todo", "status-inprogress", "status-done");
        if (statut == null) return;
        switch (statut.toLowerCase()) {
            case "terminé": case "termine": case "done":
                lblStatus.getStyleClass().add("status-done"); break;
            case "en cours": case "in progress": case "en_cours":
                lblStatus.getStyleClass().add("status-inprogress"); break;
            default:
                lblStatus.getStyleClass().add("status-todo");
        }
    }

    private void stylePriorityLabel(String priorite) {
        lblPriority.getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");
        if (priorite == null) return;
        switch (priorite.toLowerCase()) {
            case "haute": case "high": case "élevée":
                lblPriority.getStyleClass().add("priority-high"); break;
            case "moyenne": case "medium":
                lblPriority.getStyleClass().add("priority-medium"); break;
            case "basse": case "low":
                lblPriority.getStyleClass().add("priority-low"); break;
        }
    }

    private void stylePredictionProgress(double prediction) {
        progressPrediction.getStyleClass().removeAll("progress-low", "progress-medium", "progress-high");
        if (prediction < 0.4)      progressPrediction.getStyleClass().add("progress-low");
        else if (prediction < 0.7) progressPrediction.getStyleClass().add("progress-medium");
        else                       progressPrediction.getStyleClass().add("progress-high");
    }

    // ── History ────────────────────────────────────────────────────────────────

    private void loadHistory() {
        lvHistory.getItems().clear();
        try {
            List<suiviTache> history = new SuiviTacheService().showByTask(task.getId());
            if (history.isEmpty()) {
                lvHistory.getItems().add("Aucun historique disponible");
            } else {
                for (suiviTache s : history) {
                    String item = dateFormat.format(s.getDateAction()) + " : "
                            + (s.getAncienStatut() != null ? s.getAncienStatut() : "Nouveau")
                            + " → " + s.getNouveauStatut()
                            + (s.getCommentaire() != null && !s.getCommentaire().isEmpty()
                               ? " | " + s.getCommentaire() : "");
                    lvHistory.getItems().add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lvHistory.getItems().add("⚠ Erreur lors du chargement de l'historique");
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/taches/taches-content.fxml"));
            Parent tasksPage = loader.load();

            TachesController controller = loader.getController();
            controller.setSidebarController(sidebarController);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Action state machine ───────────────────────────────────────────────────

    private void performAction(String action, tache t) {
        String currentStatus = t.getStatut();
        String newStatus;
        switch (action) {
            case "start":   newStatus = "EN_COURS"; break;
            case "pause":   newStatus = "PAUSED";   break;
            case "finish":  newStatus = "TERMINE";  break;
            case "abandon": newStatus = "ABANDON";  break;
            default: return;
        }
        if (!newStatus.equals(currentStatus)) {
            try {
                // Log history
                suiviTache st = new suiviTache(t, new Date(), currentStatus, newStatus, "Action: " + action);
                new SuiviTacheService().add(st);

                // Update task — ✅ edit(t) no second id argument
                t.setStatut(newStatus);
                t.setUpdated_at(new Date());
                new TacheService().edit(t);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ── Action button visibility ───────────────────────────────────────────────

    private void updateActionButtons() {
        String status = task.getStatut();
        hideAllButtons();

        if (isTaskFinished(status)) {
            showButton(btnNoActions);
        } else {
            if (!isTaskInProgress(status) && !"PAUSED".equals(status)) showButton(btnStart);
            if (isTaskInProgress(status))                               showButton(btnPause);
            if (isTaskInProgress(status) || "PAUSED".equals(status))   showButton(btnFinish);
            showButton(btnAbandon);
        }
    }

    private void hideAllButtons() {
        hideButton(btnStart); hideButton(btnPause);
        hideButton(btnFinish); hideButton(btnAbandon); hideButton(btnNoActions);
    }

    private void hideButton(Button btn) {
        if (btn != null) { btn.setVisible(false); btn.setManaged(false); }
    }

    private void showButton(Button btn) {
        if (btn != null) { btn.setVisible(true); btn.setManaged(true); }
    }

    private boolean isTaskInProgress(String s) {
        return s != null && (s.equals("EN_COURS") || s.equalsIgnoreCase("En cours"));
    }

    private boolean isTaskFinished(String s) {
        return s != null && (s.equals("TERMINE") || s.equalsIgnoreCase("Terminé")
                          || s.equals("ABANDON") || s.equalsIgnoreCase("Abandonné"));
    }

    // ── FXML action handlers ───────────────────────────────────────────────────

    @FXML private void handleStartAction()  { performAction("start",  task); refresh(); }
    @FXML private void handlePauseAction()  { performAction("pause",  task); refresh(); }
    @FXML private void handleFinishAction() { performAction("finish", task); refresh(); }

    @FXML
    private void handleAbandonAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Abandonner la tâche");
        alert.setContentText("Êtes-vous sûr de vouloir abandonner cette tâche ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            performAction("abandon", task);
            refresh();
        }
    }

    private void refresh() {
        populateTaskDetails();
        updateActionButtons();
        loadHistory();
    }
}
