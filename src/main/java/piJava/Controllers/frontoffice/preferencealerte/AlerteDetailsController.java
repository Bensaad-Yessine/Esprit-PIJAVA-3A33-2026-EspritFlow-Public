package piJava.Controllers.frontoffice.preferencealerte;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;

import java.io.IOException;

public class AlerteDetailsController {

    @FXML private Label lblNom;
    @FXML private Label lblActive;
    @FXML private Label lblDefault;
    @FXML private Label lblEmail;
    @FXML private Label lblPush;
    @FXML private Label lblSite;
    @FXML private Label lblDelai;
    @FXML private Label lblSilenceDebut;
    @FXML private Label lblSilenceFin;

    private preferenceAlerte alerte;
    private FrontSidebarController sidebarController;

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    public void setAlerte(preferenceAlerte alerte) {
        this.alerte = alerte;
        populate();
    }

    private void populate() {
        if (alerte == null) return;
        setText(lblNom,          alerte.getNom());
        setText(lblActive,       alerte.getIs_active() ? "✅ Active" : "⏸️ Inactive");
        setText(lblDefault,      alerte.getIs_default() ? "⭐ Par défaut" : "Non");
        setText(lblEmail,        alerte.getEmail_actif() ? "✅ Oui" : "❌ Non");
        setText(lblPush,         alerte.getPush_actif() ? "✅ Oui" : "❌ Non");
        setText(lblSite,         alerte.getSite_notif_active() ? "✅ Oui" : "❌ Non");
        setText(lblDelai,        alerte.getDelai_rappel_min() + " min");
        setText(lblSilenceDebut, alerte.getHeure_silence_debut() != null ? alerte.getHeure_silence_debut().toString() : "—");
        setText(lblSilenceFin,   alerte.getHeure_silence_fin()   != null ? alerte.getHeure_silence_fin().toString()   : "—");
    }

    private void setText(Label lbl, String value) {
        if (lbl != null) lbl.setText(value != null ? value : "—");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/frontoffice/preferenceAlerte/alertes-content.fxml"));
            Parent r = loader.load();
            AlertesController ctrl = loader.getController();
            ctrl.setSidebarController(sidebarController);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
