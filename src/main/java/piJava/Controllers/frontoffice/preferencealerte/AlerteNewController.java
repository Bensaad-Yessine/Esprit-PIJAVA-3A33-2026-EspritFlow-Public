package piJava.Controllers.frontoffice.preferencealerte;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;
import piJava.utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Date;

public class AlerteNewController {

    @FXML private TextField    txtNom;
    @FXML private CheckBox     chkActive;
    @FXML private CheckBox     chkDefault;
    @FXML private CheckBox     chkEmail;
    @FXML private CheckBox     chkPush;
    @FXML private CheckBox     chkSite;
    @FXML private TextField    txtDelai;
    @FXML private TextField    txtSilenceDebut;
    @FXML private TextField    txtSilenceFin;
    @FXML private Label        lblNotification;

    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @FXML
    private void handleAdd() {
        try {
            if (txtNom.getText().isBlank()) {
                lblNotification.setText("⚠ Le nom est obligatoire.");
                return;
            }

            preferenceAlerte p = new preferenceAlerte();
            p.setNom(txtNom.getText());
            p.setIs_active(chkActive  != null && chkActive.isSelected());
            p.setIs_default(chkDefault != null && chkDefault.isSelected());
            p.setEmail_actif(chkEmail  != null && chkEmail.isSelected());
            p.setPush_actif(chkPush    != null && chkPush.isSelected());
            p.setSite_notif_active(chkSite != null && chkSite.isSelected());
            p.setDelai_rappel_min(txtDelai.getText().isBlank() ? 15 : Integer.parseInt(txtDelai.getText()));

            if (!txtSilenceDebut.getText().isBlank())
                p.setHeure_silence_debut(LocalTime.parse(txtSilenceDebut.getText()));
            else
                p.setHeure_silence_debut(LocalTime.of(22, 0));

            if (!txtSilenceFin.getText().isBlank())
                p.setHeure_silence_fin(LocalTime.parse(txtSilenceFin.getText()));
            else
                p.setHeure_silence_fin(LocalTime.of(8, 0));

            p.setDate_creation(new Date());
            p.setDate_mise_ajour(new Date());

            // ✅ User id from session
            p.setUser_id(SessionManager.getInstance().getCurrentUser().getId());

            new AlerteService().add(p);
            lblNotification.setText("✅ Préférence ajoutée avec succès !");


        } catch (NumberFormatException e) {
            lblNotification.setText("⚠ Le délai doit être un nombre entier.");
        } catch (SQLException e) {
            lblNotification.setText("⚠ Erreur DB : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {

    }
}
