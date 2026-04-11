package piJava.Controllers.frontoffice.taches;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.suiviTache;
import piJava.entities.tache;
import piJava.services.SuiviTacheService;
import piJava.services.TacheService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class TacheEditController {

    @FXML private TextField        txtTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker       dpDateDebut;
    @FXML private TextField        txtHeureDebut;
    @FXML private DatePicker       dpDateFin;
    @FXML private TextField        txtHeureFin;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextArea         txtDescription;
    @FXML private TextField        txtDuree;
    @FXML private Label            lblNotification;

    private tache currentTask;
    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    /** Called by TachesController before showing this form */
    public void setTask(tache t) {
        this.currentTask = t;
        populateForm(t);
    }

    // ── Pre-fill form with existing task values ────────────────────────────────

    private void populateForm(tache t) {
        txtTitre.setText(t.getTitre());

        if (cbType.getItems().isEmpty()) cbType.getItems().addAll("PERSONNEL", "PROFESSIONNEL", "ETUDE", "AUTRE");
        cbType.setValue(t.getType());

        if (cbPriorite.getItems().isEmpty()) cbPriorite.getItems().addAll("HAUTE", "MOYENNE", "BASSE");
        cbPriorite.setValue(t.getPriorite());

        if (cbStatut.getItems().isEmpty()) cbStatut.getItems().addAll("A_FAIRE", "EN_COURS", "PAUSED", "TERMINE", "ABANDON");
        cbStatut.setValue(t.getStatut());

        txtDescription.setText(t.getDescription() != null ? t.getDescription() : "");
        txtDuree.setText(String.valueOf(t.getDuree_estimee()));

        if (t.getDate_debut() != null) {
            LocalDateTime ldt = t.getDate_debut().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            dpDateDebut.setValue(ldt.toLocalDate());
            txtHeureDebut.setText(ldt.toLocalTime().toString());
        }
        if (t.getDate_fin() != null) {
            LocalDateTime ldt = t.getDate_fin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            dpDateFin.setValue(ldt.toLocalDate());
            txtHeureFin.setText(ldt.toLocalTime().toString());
        }
    }

    // ── Save edit ──────────────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        try {
            if (txtTitre.getText().isBlank()) {
                lblNotification.setText("⚠ Le titre est obligatoire.");
                return;
            }
            if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
                lblNotification.setText("⚠ Les dates sont obligatoires.");
                return;
            }

            // Dates
            LocalTime startTime = txtHeureDebut.getText().isBlank()
                    ? LocalTime.of(0, 0) : LocalTime.parse(txtHeureDebut.getText());
            Date dateDebut = Date.from(LocalDateTime.of(dpDateDebut.getValue(), startTime)
                    .atZone(ZoneId.systemDefault()).toInstant());

            LocalTime endTime = txtHeureFin.getText().isBlank()
                    ? LocalTime.of(23, 59) : LocalTime.parse(txtHeureFin.getText());
            Date dateFin = Date.from(LocalDateTime.of(dpDateFin.getValue(), endTime)
                    .atZone(ZoneId.systemDefault()).toInstant());

            // Track old status for suivi
            String oldStatus = currentTask.getStatut();
            String newStatus = cbStatut.getValue();

            // Apply changes to the existing task object (keeps id, user_id, etc.)
            currentTask.setTitre(txtTitre.getText());
            currentTask.setType(cbType.getValue());
            currentTask.setDate_debut(dateDebut);
            currentTask.setDate_fin(dateFin);
            currentTask.setPriorite(cbPriorite.getValue());
            currentTask.setStatut(newStatus);
            currentTask.setDescription(txtDescription.getText());
            currentTask.setDuree_estimee(txtDuree.getText().isBlank() ? 0 : Integer.parseInt(txtDuree.getText()));
            currentTask.setUpdated_at(new Date());

            // ✅ edit(t) — id comes from the object itself
            new TacheService().edit(currentTask);

            // Log status change if it changed
            if (!oldStatus.equals(newStatus)) {
                suiviTache s = new suiviTache();
                s.setTache(currentTask);
                s.setAncienStatut(oldStatus);
                s.setNouveauStatut(newStatus);
                s.setCommentaire("Modification manuelle");
                s.setDateAction(new Date());
                new SuiviTacheService().add(s);
            }

            lblNotification.setText("✅ Tâche modifiée avec succès !");

            // Go back to list
            if (sidebarController != null) {
                sidebarController.loadPage("/frontoffice/taches/taches-content.fxml", "taches");
            }

        } catch (NumberFormatException e) {
            lblNotification.setText("⚠ La durée doit être un nombre entier.");
        } catch (SQLException e) {
            lblNotification.setText("⚠ Erreur DB : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            lblNotification.setText("⚠ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        if (sidebarController != null) {
            sidebarController.loadPage("/frontoffice/taches/taches-content.fxml", "taches");
        }
    }
}
