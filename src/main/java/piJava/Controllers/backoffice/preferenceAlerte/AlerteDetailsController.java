package piJava.Controllers.backoffice.preferenceAlerte;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import piJava.Controllers.backoffice.SidebarController;
import piJava.Controllers.backoffice.preferenceAlerte.AlerteEditController;
import piJava.Controllers.backoffice.preferenceAlerte.AlertesController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AlerteDetailsController {


    @FXML
    private Label lblNom;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblDefault;

    @FXML
    private CheckBox chkEmailActif;

    @FXML
    private CheckBox chkPushActif;

    @FXML
    private CheckBox chkSiteNotif;

    @FXML
    private Label lblDelaiRappel;

    @FXML
    private Label lblHeureSilenceDebut;

    @FXML
    private Label lblHeureSilenceFin;

    @FXML
    private Label lblDateCreation;

    @FXML
    private Label lblDateMiseAjour;

    @FXML
    private HBox statusBadge;

    @FXML
    private HBox defaultBadge;

    @FXML
    private Label lblUserId;

    @FXML
    private Label lblAlerteId;
    
    private SidebarController sidebarController;

    public void setSidebarController(SidebarController sidebarcontroller) {
        this.sidebarController = sidebarcontroller;
        if (AlertesController.getCurrentAlerteForDetails() != null) {
            setAlerte(AlertesController.getCurrentAlerteForDetails());
            AlertesController.setCurrentAlerteForDetails(null);
        }
    }


    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    private preferenceAlerte alerte;
    public void setAlerte(preferenceAlerte alerte) {
        this.alerte = alerte;
        populateAlerteDetails();
    }


    private void populateAlerteDetails() {
        if (alerte == null) {
            return;
        }

        // USER INFO 👇
        lblUserId.setText("User #" + alerte.getUser_id());

        // Nom de l'alerte
        lblNom.setText(alerte.getNom() != null ? alerte.getNom() : "Sans nom");

        // Status actif/inactif
        if (alerte.getIs_active() != null && alerte.getIs_active()) {
            lblStatus.setText("Active");
            statusBadge.getStyleClass().add("badge-active");
        } else {
            lblStatus.setText("Inactive");
            statusBadge.getStyleClass().add("badge-inactive");
        }

        // Default badge
        if (alerte.getIs_default() != null && alerte.getIs_default()) {
            lblDefault.setText("Par défaut");
            defaultBadge.setVisible(true);
            defaultBadge.setManaged(true);
        } else {
            defaultBadge.setVisible(false);
            defaultBadge.setManaged(false);
        }

        chkEmailActif.setSelected(Boolean.TRUE.equals(alerte.getEmail_actif()));
        chkPushActif.setSelected(Boolean.TRUE.equals(alerte.getPush_actif()));
        chkSiteNotif.setSelected(Boolean.TRUE.equals(alerte.getSite_notif_active()));

        chkEmailActif.setDisable(true);
        chkPushActif.setDisable(true);
        chkSiteNotif.setDisable(true);

        lblDelaiRappel.setText(alerte.getDelai_rappel_min() + " minutes");

        if (alerte.getHeure_silence_debut() != null) {
            lblHeureSilenceDebut.setText(alerte.getHeure_silence_debut().format(TIME_FORMATTER));
        } else {
            lblHeureSilenceDebut.setText("Non définie");
        }

        if (alerte.getHeure_silence_fin() != null) {
            lblHeureSilenceFin.setText(alerte.getHeure_silence_fin().format(TIME_FORMATTER));
        } else {
            lblHeureSilenceFin.setText("Non définie");
        }

        if (alerte.getDate_creation() != null) {
            lblDateCreation.setText(DATE_FORMATTER.format(alerte.getDate_creation()));
        } else {
            lblDateCreation.setText("Inconnue");
        }

        if (alerte.getDate_mise_ajour() != null) {
            lblDateMiseAjour.setText(DATE_FORMATTER.format(alerte.getDate_mise_ajour()));
        } else {
            lblDateMiseAjour.setText("Jamais");
        }

        lblAlerteId.setText("#" + alerte.getId());
    }

    @FXML
    private void handleRetour() {
        if (sidebarController != null) {
            sidebarController.goToNotifications();
        } else {
            System.err.println("Sidebar controller not set!");
        }
    }

    @FXML
    private void handleModifier() {
        try {
            AlertesController.setCurrentAlerteForEdit(alerte);
            if (sidebarController != null) {
                sidebarController.loadView("/backoffice/preferenceAlerte/alerte-edit.fxml");
            } else {
                System.out.println("impossible de naviguer vers la page d'édition de l'alerte.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de la page d'édition de l'alerte : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Confirmation de la Suppression");
        confirmation.setContentText("Etes-vous sûr de vouloir supprimer cette alerte ?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                AlerteService as = new  AlerteService();
                as.delete(alerte.getId());
                Alert info  = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Suppression réussie");
                info.setHeaderText("Suppression de la preference reussie");
                info.setContentText("Vous seriez redige vers la page des alertes");
                info.showAndWait();
                this.handleRetour();
            } catch (SQLException e) {
                Alert info  = new Alert(Alert.AlertType.ERROR);
                info.setTitle("Erreur de suppression");
                info.setHeaderText("Erreur lors de la suppression de la preference");
                info.setContentText("Une erreur est survenue lors de la suppression de la preference : " + e.getMessage());
                info.showAndWait();
            }
        }

    }

//    @FXML
//    public void handleActive() {
//        try {
//            AlerteService as = new AlerteService();
//            as.setActive(alerte.getId(), currentUserId);
//
//            Alert info = new Alert(Alert.AlertType.INFORMATION);
//            info.setTitle("✔ Succès");
//            info.setHeaderText("Activation réussie");
//            info.setContentText("Cette préférence est désormais active.");
//            info.showAndWait();
//
//        } catch (SQLException e) {
//            Alert error = new Alert(Alert.AlertType.ERROR);
//            error.setTitle("❌ Erreur");
//            error.setHeaderText("Échec de l'activation");
//            error.setContentText(e.getMessage());
//            error.showAndWait();
//        }
//    }
//

}
