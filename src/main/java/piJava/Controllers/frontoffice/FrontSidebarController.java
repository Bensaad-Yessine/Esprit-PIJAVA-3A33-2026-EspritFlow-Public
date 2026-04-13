package piJava.Controllers.frontoffice;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import piJava.utils.SessionManager;
import javafx.scene.control.Label;
public class FrontSidebarController {

    @FXML private Button dashboardBtn;
    @FXML private Button tachesBtn;
    @FXML private Button classesBtn;
    @FXML private Button matieresBtn;
    @FXML private Button objectifsBtn;
    @FXML private Button suivisBtn;
    @FXML private Button enseignantsBtn;
    @FXML private Button emploiBtn;
    @FXML private Button notesBtn;
    @FXML private Button notificationsBtn;

    private StackPane contentArea;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public void setActivePage(String page) {
        dashboardBtn.getStyleClass().remove("menu-item-active");
        tachesBtn.getStyleClass().remove("menu-item-active");
        classesBtn.getStyleClass().remove("menu-item-active");
        objectifsBtn.getStyleClass().remove("menu-item-active");
        suivisBtn.getStyleClass().remove("menu-item-active");
        matieresBtn.getStyleClass().remove("menu-item-active");
        enseignantsBtn.getStyleClass().remove("menu-item-active");
        emploiBtn.getStyleClass().remove("menu-item-active");
        notesBtn.getStyleClass().remove("menu-item-active");
        notificationsBtn.getStyleClass().remove("menu-item-active");

        switch (page) {
            case "dashboard" -> dashboardBtn.getStyleClass().add("menu-item-active");
            case "taches" -> tachesBtn.getStyleClass().add("menu-item-active");
            case "classes" -> classesBtn.getStyleClass().add("menu-item-active");
            case "matieres" -> matieresBtn.getStyleClass().add("menu-item-active");
            case "objectifs" -> objectifsBtn.getStyleClass().add("menu-item-active");
            case "suivis" -> suivisBtn.getStyleClass().add("menu-item-active");
            case "enseignants" -> enseignantsBtn.getStyleClass().add("menu-item-active");
            case "emploi" -> emploiBtn.getStyleClass().add("menu-item-active");
            case "notes" -> notesBtn.getStyleClass().add("menu-item-active");
            case "alertes" -> notificationsBtn.getStyleClass().add("menu-item-active");
        }
    }

    public void loadPage(String fxmlFile, String activePage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();

            Object controller = loader.getController();

            if (controller instanceof piJava.Controllers.frontoffice.taches.TachesController tc) {
                tc.setSidebarController(this);
            }

            if (controller instanceof piJava.Controllers.frontoffice.preferencealerte.AlertesController ac) {
                ac.setSidebarController(this);
            }

            if (controller instanceof piJava.Controllers.frontoffice.objectifsante.AfficherObjectifsController oc) {
                oc.setSidebarController(this);
                oc.setContentArea(contentArea);
            }

            if (controller instanceof piJava.Controllers.frontoffice.suivibienetre.AfficherSuivisController sc) {
                sc.setSidebarController(this);
                sc.setContentArea(contentArea);
            }

            contentArea.getChildren().setAll(page);
            setActivePage(activePage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToDashboard() {
        loadPage("/frontoffice/dashboard/dashboard-content.fxml", "dashboard");
    }

    @FXML
    void goToTaches() {
        loadPage("/frontoffice/taches/taches-content.fxml", "taches");
    }

    @FXML
    void goToClasses() {
        loadPage("/frontoffice/classes/classes-content.fxml", "classes");
    }

    @FXML
    public void goToObjectifs() {
        loadPage("/frontoffice/objectifsante/AfficherObjectifs.fxml", "objectifs");
    }

    @FXML
    public void goToSuivis() {
        loadPage("/frontoffice/suivibienetre/AfficherSuivis.fxml", "suivis");
    }

    @FXML
    void goToMatieres() {
        loadPage("/frontoffice/matieres/matieres-content.fxml", "matieres");
    }

    @FXML
    void goToEnseignants() {
        loadPage("/frontoffice/enseignants/enseignants-content.fxml", "enseignants");
    }

    @FXML
    void goToEmploi() {
        loadPage("/frontoffice/emploi/emploi-content.fxml", "emploi");
    }

    @FXML
    void goToNotes() {
        loadPage("/frontoffice/notes/notes-content.fxml", "notes");
    }

    @FXML
    void goToNotifications() {
        loadPage("/frontoffice/preferenceAlerte/alerte-content.fxml", "alertes");
    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            SessionManager.getInstance().logout();

            Parent root = FXMLLoader.load(getClass().getResource("/login/login.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}