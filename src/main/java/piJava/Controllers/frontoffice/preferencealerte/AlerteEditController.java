package piJava.Controllers.frontoffice.preferencealerte;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Date;

public class AlerteEditController {

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

    private preferenceAlerte alerte;
    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setAlerte(preferenceAlerte alerte) {
        this.alerte = alerte;
        populateForm();
    }

    private void populateForm() {
        if (alerte == null) return;
        setText(txtNom, alerte.getNom());
        setCheck(chkActive,  alerte.getIs_active());
        setCheck(chkDefault, alerte.getIs_default());
        setCheck(chkEmail,   alerte.getEmail_actif());
        setCheck(chkPush,    alerte.getPush_actif());
        setCheck(chkSite,    alerte.getSite_notif_active());
        setText(txtDelai, String.valueOf(alerte.getDelai_rappel_min()));
        setText(txtSilenceDebut, alerte.getHeure_silence_debut() != null ? alerte.getHeure_silence_debut().toString() : "");
        setText(txtSilenceFin,   alerte.getHeure_silence_fin()   != null ? alerte.getHeure_silence_fin().toString()   : "");
    }

    @FXML
    private void handleSave() {
        try {
            if (txtNom.getText().isBlank()) {
                lblNotification.setText("⚠ Le nom est obligatoire.");
                return;
            }

            alerte.setNom(txtNom.getText());
            alerte.setIs_active(chkActive != null && chkActive.isSelected());
            alerte.setIs_default(chkDefault != null && chkDefault.isSelected());
            alerte.setEmail_actif(chkEmail != null && chkEmail.isSelected());
            alerte.setPush_actif(chkPush != null && chkPush.isSelected());
            alerte.setSite_notif_active(chkSite != null && chkSite.isSelected());
            alerte.setDelai_rappel_min(txtDelai.getText().isBlank() ? 0 : Integer.parseInt(txtDelai.getText()));

            if (!txtSilenceDebut.getText().isBlank())
                alerte.setHeure_silence_debut(LocalTime.parse(txtSilenceDebut.getText()));
            if (!txtSilenceFin.getText().isBlank())
                alerte.setHeure_silence_fin(LocalTime.parse(txtSilenceFin.getText()));

            alerte.setDate_mise_ajour(new Date());

            // ✅ edit(p) — no second id argument
            new AlerteService().edit(alerte);

            lblNotification.setText("✅ Préférence modifiée avec succès !");

            if (sidebarController != null) {
                sidebarController.loadPage("/frontoffice/preferenceAlerte/alerte-content.fxml", "alertes");
            }

        } catch (NumberFormatException e) {
            lblNotification.setText("⚠ Le délai doit être un nombre entier.");
        } catch (SQLException e) {
            lblNotification.setText("⚠ Erreur DB : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        if (sidebarController != null) {
            sidebarController.loadPage("/frontoffice/preferenceAlerte/alerte-content.fxml", "alertes");
        }
    }

    private void setText(TextField f, String v)  { if (f != null) f.setText(v != null ? v : ""); }
    private void setCheck(CheckBox c, boolean v) { if (c != null) c.setSelected(v); }
}
