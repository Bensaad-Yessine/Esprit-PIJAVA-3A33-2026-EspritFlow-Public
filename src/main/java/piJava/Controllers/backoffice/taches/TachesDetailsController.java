package piJava.Controllers.backoffice.taches;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import piJava.Controllers.backoffice.SidebarController;
import piJava.Controllers.backoffice.taches.TachesController;
import piJava.entities.suiviTache;
import piJava.entities.tache;
import piJava.services.SuiviTacheService;
import piJava.services.TacheService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TachesDetailsController {
    
    @FXML
    private Label lblTitle;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblPriority;

    @FXML
    private Label lblType;

    @FXML
    private Label lblDateDebut;

    @FXML
    private Label lblDateFin;

    @FXML
    private Label lblDateEcheance;

    @FXML
    private Label lblDureeEstimee;

    @FXML
    private Label lblPrediction;

    @FXML
    private ProgressBar progressPrediction;

    @FXML
    private ListView<String> lvHistory;

    @FXML
    private Button btnBack;

    // Action buttons
    @FXML
    private Button btnStart;

    @FXML
    private Button btnPause;

    @FXML
    private Button btnFinish;

    @FXML
    private Button btnAbandon;

    @FXML
    private Button btnNoActions;

    @FXML
    private Label lblUserId;

    private tache task;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private SidebarController sidebarController;
    public void setSidebarController(SidebarController sidebarController) {
        this.sidebarController = sidebarController;
        // Retrieve the task that was set before loading this view
        if (TachesController.getCurrentTaskForDetails() != null) {
            setTask(TachesController.getCurrentTaskForDetails());
            TachesController.setCurrentTaskForDetails(null); // Clear it after use
        }
    }


    public void setTask(tache task) {
        this.task = task;
        populateTaskDetails();
        loadHistory();
        updateActionButtons();
    }

    private void populateTaskDetails() {
        // Basic info
        lblTitle.setText(task.getTitre());
        lblDescription.setText(task.getDescription());
        lblType.setText(task.getType());

        lblUserId.setText("User #" + task.getUser_id());

        // Status with styling
        lblStatus.setText(task.getStatut());
        styleStatusLabel(task.getStatut());

        // Priority with styling
        lblPriority.setText(task.getPriorite());
        stylePriorityLabel(task.getPriorite());

        // Dates
        lblDateDebut.setText(task.getDate_debut() != null ?
                dateFormat.format(task.getDate_debut()) : "Non définie");
        lblDateFin.setText(task.getDate_fin() != null ?
                dateFormat.format(task.getDate_fin()) : "Non définie");
        lblDateEcheance.setText(task.getDate_echeance() != null ?
                dateFormat.format(task.getDate_echeance()) : "Non définie");

        // Duration
        if (task.getDuree_estimee() > 0) {
            lblDureeEstimee.setText(task.getDuree_estimee() + " heures");
        } else {
            lblDureeEstimee.setText("Non estimée");
        }

        // Prediction
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

        if (statut != null) {
            switch (statut.toLowerCase()) {
                case "terminé":
                case "termine":
                case "done":
                    lblStatus.getStyleClass().add("status-done");
                    break;
                case "en cours":
                case "in progress":
                case "en_cours":
                    lblStatus.getStyleClass().add("status-inprogress");
                    break;
                default:
                    lblStatus.getStyleClass().add("status-todo");
            }
        }
    }

    private void stylePriorityLabel(String priorite) {
        lblPriority.getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");

        if (priorite != null) {
            switch (priorite.toLowerCase()) {
                case "haute":
                case "high":
                case "élevée":
                    lblPriority.getStyleClass().add("priority-high");
                    break;
                case "moyenne":
                case "medium":
                    lblPriority.getStyleClass().add("priority-medium");
                    break;
                case "basse":
                case "low":
                    lblPriority.getStyleClass().add("priority-low");
                    break;
            }
        }
    }

    private void stylePredictionProgress(double prediction) {
        progressPrediction.getStyleClass().removeAll("progress-low", "progress-medium", "progress-high");

        if (prediction < 0.4) {
            progressPrediction.getStyleClass().add("progress-low");
        } else if (prediction < 0.7) {
            progressPrediction.getStyleClass().add("progress-medium");
        } else {
            progressPrediction.getStyleClass().add("progress-high");
        }
    }

    private void loadHistory() {
        lvHistory.getItems().clear();
        SuiviTacheService sts = new SuiviTacheService();
        try {
            List<suiviTache> history = sts.showByTask(task.getId());

            if (history.isEmpty()) {
                lvHistory.getItems().add("Aucun historique disponible");
            } else {
                for (suiviTache s : history) {
                    String item = dateFormat.format(s.getDateAction()) + " : "
                            + (s.getAncienStatut() != null ? s.getAncienStatut() : "Nouveau")
                            + " → "
                            + s.getNouveauStatut()
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

    @FXML
    private void handleBack() {
        if (sidebarController != null) {
            sidebarController.goToTaches();
        } else {
            System.err.println("Sidebar controller not set!");
        }
    }

    private void Action(String action, tache t) {
        // action in ['start','pause','finish','abandon']
        String currentStatus = t.getStatut();
        String newStatus = "";
        switch (action) {
            case "start":
                newStatus = "EN_COURS";
                break;
            case "pause":
                newStatus = "PAUSED";
                break;
            case "finish":
                newStatus = "TERMINE";
                break;
            case "abandon":
                newStatus = "ABANDON";
                break;
        }
        List<String> status = List.of("EN_COURS", "PAUSED", "TERMINE", "ABANDON");
        if (!newStatus.equals(currentStatus) && status.contains(newStatus)) {
            suiviTache st = new suiviTache(t, new Date(), currentStatus, newStatus, "Action: " + action);
            SuiviTacheService sts = new SuiviTacheService();
            TacheService ts = new TacheService();
            try {
                sts.add(st);
                t.setStatut(newStatus);
                t.setUpdated_at(new Date());
                ts.edit(t);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // ========== ACTION BUTTON METHODS ==========

    private void updateActionButtons() {
        String status = task.getStatut();

        // Hide all buttons first
        hideAllButtons();

        // Show appropriate buttons based on status
        if (isTaskFinished(status)) {
            showButton(btnNoActions);
        } else {
            // Start button - show if not started and not finished
            if (!isTaskInProgress(status) && !isTaskFinished(status) && !status.equals("PAUSED")) {
                showButton(btnStart);
            }

            // Pause button - show only if in progress
            if (isTaskInProgress(status)) {
                showButton(btnPause);
            }

            // Finish button - show if in progress or paused
            if (isTaskInProgress(status) || status.equals("PAUSED")) {
                showButton(btnFinish);
            }

            // Abandon button - show if not finished
            if (!isTaskFinished(status)) {
                showButton(btnAbandon);
            }
        }
    }

    private void hideAllButtons() {
        hideButton(btnStart);
        hideButton(btnPause);
        hideButton(btnFinish);
        hideButton(btnAbandon);
        hideButton(btnNoActions);
    }

    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    private void showButton(Button btn) {
        if (btn != null) {
            btn.setVisible(true);
            btn.setManaged(true);
        }
    }

    private boolean isTaskInProgress(String status) {
        return status != null && (status.equals("EN_COURS") || status.equalsIgnoreCase("En cours"));
    }

    private boolean isTaskFinished(String status) {
        return status != null && (
                status.equals("TERMINE") || status.equalsIgnoreCase("Terminé") ||
                        status.equals("ABANDON") || status.equalsIgnoreCase("Abandonné")
        );
    }

    @FXML
    private void handleStartAction() {
        Action("start", task);
        populateTaskDetails();
        updateActionButtons();
        loadHistory();
    }

    @FXML
    private void handlePauseAction() {
        Action("pause", task);
        populateTaskDetails();
        updateActionButtons();
        loadHistory();
    }

    @FXML
    private void handleFinishAction() {
        Action("finish", task);
        populateTaskDetails();
        updateActionButtons();
        loadHistory();
    }

    @FXML
    private void handleAbandonAction() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Abandonner la tâche");
        alert.setContentText("Êtes-vous sûr de vouloir abandonner cette tâche ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Action("abandon", task);
            populateTaskDetails();
            updateActionButtons();
            loadHistory();
        }
    }

}
