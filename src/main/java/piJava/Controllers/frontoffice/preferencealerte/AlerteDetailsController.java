package piJava.Controllers.frontoffice.preferencealerte;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import piJava.Controllers.frontoffice.FrontSidebarController;
import piJava.entities.preferenceAlerte;
import piJava.services.AlerteService;
import piJava.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AlerteDetailsController implements Initializable {

    // Static variable to pass alerte from AlertesController
    public static preferenceAlerte currentAlerte = null;

    private final int currentUserId = SessionManager.getInstance().getCurrentUser().getId();

    @FXML
    private Label lblNom;

    @FXML
    private Label lblDescription;

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

    private preferenceAlerte alerte;
    private FrontSidebarController sidebarController;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public void setAlerte(preferenceAlerte alerte) {
        this.alerte = alerte;
        populateAlerteDetails();
    }

    public void setSidebarController(FrontSidebarController sidebarController) {
        this.sidebarController = sidebarController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Retrieve the alerte that was set by AlertesController before loading this view
        if (currentAlerte != null) {
            setAlerte(currentAlerte);
            currentAlerte = null; // Clear it after use
        }
    }

    private void populateAlerteDetails() {
        if (alerte == null) {
            return;
        }

        // Nom de l'alerte
        lblNom.setText(alerte.getNom() != null ? alerte.getNom() : "Sans nom");

        // Description
        if (lblDescription != null) {
            lblDescription.setText(alerte.getDescription() != null ? alerte.getDescription() : "Aucune description");
        }

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

        // Canaux de notification
        chkEmailActif.setSelected(alerte.getEmail_actif() != null && alerte.getEmail_actif());
        chkPushActif.setSelected(alerte.getPush_actif() != null && alerte.getPush_actif());
        chkSiteNotif.setSelected(alerte.getSite_notif_active() != null && alerte.getSite_notif_active());

        // Désactiver l'édition (affichage seulement)
        chkEmailActif.setDisable(true);
        chkPushActif.setDisable(true);
        chkSiteNotif.setDisable(true);

        // Délai de rappel
        lblDelaiRappel.setText(alerte.getDelai_rappel_min() + " minutes");

        // Heures de silence
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

        // Dates
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
    }

    @FXML
    private void handleRetour() {
        if (sidebarController != null) {
            sidebarController.goToNotifications();
        }
    }

    @FXML
    private void handleModifier() {
        try {
            // Store the alerte temporarily so AlerteEditController can access it
            AlerteEditController.currentAlerte = alerte;
            sidebarController.loadView("/frontoffice/preferenceAlerte/alerte-edit.fxml");
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
                AlerteService as = new AlerteService();
                as.delete(alerte.getId());
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Suppression réussie");
                info.setHeaderText("Suppression de la preference réussie");
                info.setContentText("L'alerte a été supprimée avec succès.");
                info.showAndWait();
                this.handleRetour();
            } catch (SQLException e) {
                Alert info = new Alert(Alert.AlertType.ERROR);
                info.setTitle("Erreur de suppression");
                info.setHeaderText("Erreur lors de la suppression de la preference");
                info.setContentText("Une erreur est survenue lors de la suppression de la preference : " + e.getMessage());
                info.showAndWait();
            }
        }
    }

    @FXML
    public void handleActive() {
        try {
            AlerteService as = new AlerteService();
            as.setActive(alerte.getId(), currentUserId);

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("✔ Succès");
            info.setHeaderText("Activation réussie");
            info.setContentText("Cette préférence est désormais active.");
            info.showAndWait();

        } catch (SQLException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("❌ Erreur");
            error.setHeaderText("Échec de l'activation");
            error.setContentText(e.getMessage());
            error.showAndWait();
        }
    }
}