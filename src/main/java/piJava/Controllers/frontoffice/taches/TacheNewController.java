package piJava.Controllers.frontoffice.taches;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.suiviTache;
import piJava.entities.tache;
import piJava.services.SuiviTacheService;
import piJava.services.TacheService;
import piJava.utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class TacheNewController {

    @FXML private TextField    txtTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker   dpDateDebut;
    @FXML private TextField    txtHeureDebut;
    @FXML private DatePicker   dpDateFin;
    @FXML private TextField    txtHeureFin;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextArea     txtDescription;
    @FXML private TextField    txtDuree;
    @FXML private Label        lblNotification;

    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @FXML
    private void handleAdd() {
        try {
            // ── Validate required fields ────────────────────────────────────
            if (txtTitre.getText().isBlank()) {
                lblNotification.setText("⚠ Le titre est obligatoire.");
                return;
            }
            if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
                lblNotification.setText("⚠ Les dates sont obligatoires.");
                return;
            }

            // ── Parse dates ─────────────────────────────────────────────────
            LocalDate startDate = dpDateDebut.getValue();
            LocalTime startTime = txtHeureDebut.getText().isBlank()
                    ? LocalTime.of(0, 0)
                    : LocalTime.parse(txtHeureDebut.getText());
            Date dateDebut = Date.from(LocalDateTime.of(startDate, startTime)
                    .atZone(ZoneId.systemDefault()).toInstant());

            LocalDate endDate = dpDateFin.getValue();
            LocalTime endTime = txtHeureFin.getText().isBlank()
                    ? LocalTime.of(23, 59)
                    : LocalTime.parse(txtHeureFin.getText());
            Date dateFin = Date.from(LocalDateTime.of(endDate, endTime)
                    .atZone(ZoneId.systemDefault()).toInstant());

            // ── Build tache ─────────────────────────────────────────────────
            tache t = new tache();
            t.setTitre(txtTitre.getText());
            t.setType(cbType.getValue());
            t.setDate_debut(dateDebut);
            t.setDate_fin(dateFin);
            t.setPriorite(cbPriorite.getValue());
            t.setStatut(cbStatut.getValue() != null ? cbStatut.getValue() : "A_FAIRE");
            t.setDescription(txtDescription.getText());
            t.setDuree_estimee(txtDuree.getText().isBlank() ? 0 : Integer.parseInt(txtDuree.getText()));
            t.setCreated_at(new Date());
            t.setUpdated_at(new Date());
            t.setPrediction(0);

            // ✅ User id from session
            t.setUser_id(SessionManager.getInstance().getCurrentUser().getId());

            // ── Save ────────────────────────────────────────────────────────
            TacheService ts = new TacheService();
            ts.add(t); // sets t.getId() after insert

            // ── Log initial suivi ───────────────────────────────────────────
            suiviTache s = new suiviTache();
            s.setTache(t);
            s.setAncienStatut("");
            s.setNouveauStatut(t.getStatut());
            s.setCommentaire("Création de la tâche");
            s.setDateAction(new Date());
            new SuiviTacheService().add(s);

            lblNotification.setText("✅ Tâche ajoutée avec succès !");

            // ── Go back to tasks list ───────────────────────────────────────


        } catch (NumberFormatException e) {
            lblNotification.setText("⚠ La durée doit être un nombre entier.");
        } catch (Exception e) {
            lblNotification.setText("⚠ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
